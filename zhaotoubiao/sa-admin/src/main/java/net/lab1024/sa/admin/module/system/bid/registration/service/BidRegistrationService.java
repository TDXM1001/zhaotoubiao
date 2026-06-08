package net.lab1024.sa.admin.module.system.bid.registration.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import net.lab1024.sa.admin.module.system.bid.common.constant.BidLotStatusEnum;
import net.lab1024.sa.admin.module.system.bid.common.constant.BidRegistrationStatusEnum;
import net.lab1024.sa.admin.module.system.bid.common.service.BidWorkflowHistoryService;
import net.lab1024.sa.admin.module.system.bid.lot.dao.BidLotDao;
import net.lab1024.sa.admin.module.system.bid.lot.domain.entity.BidLotEntity;
import net.lab1024.sa.admin.module.system.bid.project.dao.BidProjectDao;
import net.lab1024.sa.admin.module.system.bid.project.domain.entity.BidProjectEntity;
import net.lab1024.sa.admin.module.system.bid.registration.dao.BidRegistrationDao;
import net.lab1024.sa.admin.module.system.bid.registration.domain.entity.BidRegistrationEntity;
import net.lab1024.sa.admin.module.system.bid.registration.domain.form.BidRegistrationActionForm;
import net.lab1024.sa.admin.module.system.bid.registration.domain.form.BidRegistrationAddForm;
import net.lab1024.sa.admin.module.system.bid.registration.domain.form.BidRegistrationQueryForm;
import net.lab1024.sa.admin.module.system.bid.registration.domain.vo.BidRegistrationVO;
import net.lab1024.sa.base.common.code.UserErrorCode;
import net.lab1024.sa.base.common.domain.PageResult;
import net.lab1024.sa.base.common.domain.ResponseDTO;
import net.lab1024.sa.base.common.util.SmartBeanUtil;
import net.lab1024.sa.base.common.util.SmartPageUtil;
import net.lab1024.sa.base.common.util.SmartRequestUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 供应商报名 Service
 *
 * @author Codex
 * @date 2026-06-07
 */
@Service
public class BidRegistrationService {

    @Resource
    private BidRegistrationDao bidRegistrationDao;

    @Resource
    private BidProjectDao bidProjectDao;

    @Resource
    private BidLotDao bidLotDao;

    @Resource
    private BidWorkflowHistoryService bidWorkflowHistoryService;

    /**
     * 分页查询
     */
    public PageResult<BidRegistrationVO> queryPage(BidRegistrationQueryForm queryForm) {
        queryForm.setDeletedFlag(Boolean.FALSE);
        Page<?> page = SmartPageUtil.convert2PageQuery(queryForm);
        List<BidRegistrationVO> list = bidRegistrationDao.queryPage(page, queryForm);
        if (list != null) {
            list.forEach(this::fillAllowedActions);
        }
        return SmartPageUtil.convert2PageResult(page, list);
    }

    /**
     * 查询详情
     */
    public ResponseDTO<BidRegistrationVO> getDetail(Long registrationId) {
        BidRegistrationVO detail = bidRegistrationDao.getDetail(registrationId);
        if (detail == null) {
            return ResponseDTO.error(UserErrorCode.DATA_NOT_EXIST);
        }
        fillAllowedActions(detail);
        return ResponseDTO.ok(detail);
    }

    /**
     * 新增报名
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> add(BidRegistrationAddForm addForm) {
        ResponseDTO<String> validateResult = validateRegistrationForm(addForm);
        if (!validateResult.getOk()) {
            return validateResult;
        }
        if (existsActiveRegistration(addForm.getLotId(), addForm.getSupplierCreditCode())) {
            return ResponseDTO.userErrorParam("该供应商已完成当前标段报名，请勿重复提交");
        }

        BidRegistrationEntity entity = SmartBeanUtil.copy(addForm, BidRegistrationEntity.class);
        entity.setSupplierNameSnapshot(StringUtils.trim(addForm.getSupplierNameSnapshot()));
        entity.setSupplierCreditCode(StringUtils.upperCase(StringUtils.trim(addForm.getSupplierCreditCode())));
        entity.setStatus(BidRegistrationStatusEnum.SUBMITTED.getCode());
        entity.setSubmitTime(LocalDateTime.now());
        entity.setDeletedFlag(Boolean.FALSE);
        entity.setVersion(0);
        entity.setCreateUserId(SmartRequestUtil.getRequestUserId());
        entity.setUpdateUserId(SmartRequestUtil.getRequestUserId());
        bidRegistrationDao.insert(entity);

        bidWorkflowHistoryService.recordRegistrationAction(entity.getRegistrationId(), entity.getProjectId(), entity.getLotId(),
                null, entity.getStatus(), "create-registration", "新增供应商报名", SmartRequestUtil.getRequestUser(), entity);
        return ResponseDTO.ok(String.valueOf(entity.getRegistrationId()));
    }

    /**
     * 取消报名
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> cancel(BidRegistrationActionForm actionForm) {
        BidRegistrationEntity current = getCurrent(actionForm.getRegistrationId());
        if (current == null) {
            return ResponseDTO.error(UserErrorCode.DATA_NOT_EXIST);
        }
        if (!Objects.equals(current.getVersion(), actionForm.getVersion())) {
            return ResponseDTO.userErrorParam("数据已更新，请刷新后重试");
        }
        if (!Objects.equals(current.getStatus(), BidRegistrationStatusEnum.SUBMITTED.getCode())
                && !Objects.equals(current.getStatus(), BidRegistrationStatusEnum.REJECTED.getCode())) {
            return ResponseDTO.userErrorParam("只有待审核或已驳回报名才能取消");
        }

        BidRegistrationEntity updateEntity = new BidRegistrationEntity();
        updateEntity.setRegistrationId(current.getRegistrationId());
        updateEntity.setVersion(actionForm.getVersion());
        updateEntity.setStatus(BidRegistrationStatusEnum.CANCELLED.getCode());
        updateEntity.setCancelTime(LocalDateTime.now());
        updateEntity.setCancelReason(StringUtils.trimToEmpty(actionForm.getRemark()));
        updateEntity.setUpdateUserId(SmartRequestUtil.getRequestUserId());
        Integer updateCount = bidRegistrationDao.updateWithVersion(updateEntity);
        if (updateCount == null || updateCount == 0) {
            return ResponseDTO.userErrorParam("数据已更新，请刷新后重试");
        }

        bidWorkflowHistoryService.recordRegistrationAction(current.getRegistrationId(), current.getProjectId(), current.getLotId(),
                current.getStatus(), BidRegistrationStatusEnum.CANCELLED.getCode(), "cancel-registration", actionForm.getRemark(), SmartRequestUtil.getRequestUser(), updateEntity);
        return ResponseDTO.ok();
    }

    /**
     * 报名审核通过
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> approve(BidRegistrationActionForm actionForm) {
        BidRegistrationEntity current = getCurrent(actionForm.getRegistrationId());
        if (current == null) {
            return ResponseDTO.error(UserErrorCode.DATA_NOT_EXIST);
        }
        if (!Objects.equals(current.getVersion(), actionForm.getVersion())) {
            return ResponseDTO.userErrorParam("数据已更新，请刷新后重试");
        }
        if (!Objects.equals(current.getStatus(), BidRegistrationStatusEnum.SUBMITTED.getCode())) {
            return ResponseDTO.userErrorParam("只有待审核报名才能审核通过");
        }

        BidRegistrationEntity updateEntity = new BidRegistrationEntity();
        updateEntity.setRegistrationId(current.getRegistrationId());
        updateEntity.setVersion(actionForm.getVersion());
        updateEntity.setStatus(BidRegistrationStatusEnum.QUALIFIED.getCode());
        updateEntity.setQualifiedResult(BidRegistrationStatusEnum.QUALIFIED.getCode());
        updateEntity.setQualifiedTime(LocalDateTime.now());
        updateEntity.setQualifiedBy(SmartRequestUtil.getRequestUserId());
        updateEntity.setRejectReason("");
        updateEntity.setUpdateUserId(SmartRequestUtil.getRequestUserId());
        Integer updateCount = bidRegistrationDao.updateWithVersion(updateEntity);
        if (updateCount == null || updateCount == 0) {
            return ResponseDTO.userErrorParam("数据已更新，请刷新后重试");
        }

        bidWorkflowHistoryService.recordRegistrationAction(current.getRegistrationId(), current.getProjectId(), current.getLotId(),
                current.getStatus(), BidRegistrationStatusEnum.QUALIFIED.getCode(), "approve-registration", actionForm.getRemark(), SmartRequestUtil.getRequestUser(), updateEntity);
        return ResponseDTO.ok();
    }

    /**
     * 报名驳回
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> reject(BidRegistrationActionForm actionForm) {
        BidRegistrationEntity current = getCurrent(actionForm.getRegistrationId());
        if (current == null) {
            return ResponseDTO.error(UserErrorCode.DATA_NOT_EXIST);
        }
        if (!Objects.equals(current.getVersion(), actionForm.getVersion())) {
            return ResponseDTO.userErrorParam("数据已更新，请刷新后重试");
        }
        if (!Objects.equals(current.getStatus(), BidRegistrationStatusEnum.SUBMITTED.getCode())) {
            return ResponseDTO.userErrorParam("只有待审核报名才能驳回");
        }
        if (StringUtils.isBlank(actionForm.getRemark())) {
            return ResponseDTO.userErrorParam("驳回报名时必须填写驳回原因");
        }

        BidRegistrationEntity updateEntity = new BidRegistrationEntity();
        updateEntity.setRegistrationId(current.getRegistrationId());
        updateEntity.setVersion(actionForm.getVersion());
        updateEntity.setStatus(BidRegistrationStatusEnum.REJECTED.getCode());
        updateEntity.setQualifiedResult(BidRegistrationStatusEnum.REJECTED.getCode());
        updateEntity.setQualifiedTime(LocalDateTime.now());
        updateEntity.setQualifiedBy(SmartRequestUtil.getRequestUserId());
        updateEntity.setRejectReason(StringUtils.trim(actionForm.getRemark()));
        updateEntity.setUpdateUserId(SmartRequestUtil.getRequestUserId());
        Integer updateCount = bidRegistrationDao.updateWithVersion(updateEntity);
        if (updateCount == null || updateCount == 0) {
            return ResponseDTO.userErrorParam("数据已更新，请刷新后重试");
        }

        bidWorkflowHistoryService.recordRegistrationAction(current.getRegistrationId(), current.getProjectId(), current.getLotId(),
                current.getStatus(), BidRegistrationStatusEnum.REJECTED.getCode(), "reject-registration", actionForm.getRemark(), SmartRequestUtil.getRequestUser(), updateEntity);
        return ResponseDTO.ok();
    }

    private ResponseDTO<String> validateRegistrationForm(BidRegistrationAddForm form) {
        BidProjectEntity project = bidProjectDao.selectById(form.getProjectId());
        if (project == null || Boolean.TRUE.equals(project.getDeletedFlag())) {
            return ResponseDTO.userErrorParam("所属项目不存在");
        }

        BidLotEntity lot = bidLotDao.selectById(form.getLotId());
        if (lot == null || Boolean.TRUE.equals(lot.getDeletedFlag())) {
            return ResponseDTO.userErrorParam("所属标段不存在");
        }
        if (!Objects.equals(lot.getProjectId(), form.getProjectId())) {
            return ResponseDTO.userErrorParam("标段不属于当前项目");
        }
        if (!Objects.equals(lot.getStatus(), BidLotStatusEnum.BIDDING.getCode())) {
            return ResponseDTO.userErrorParam("只有投标中的标段才允许报名");
        }
        return ResponseDTO.ok();
    }

    private boolean existsActiveRegistration(Long lotId, String supplierCreditCode) {
        Long count = bidRegistrationDao.selectCount(Wrappers.<BidRegistrationEntity>lambdaQuery()
                .eq(BidRegistrationEntity::getLotId, lotId)
                .eq(BidRegistrationEntity::getSupplierCreditCode, StringUtils.upperCase(StringUtils.trim(supplierCreditCode)))
                .eq(BidRegistrationEntity::getDeletedFlag, Boolean.FALSE));
        return count != null && count > 0;
    }

    private BidRegistrationEntity getCurrent(Long registrationId) {
        BidRegistrationEntity current = bidRegistrationDao.selectById(registrationId);
        if (current == null || Boolean.TRUE.equals(current.getDeletedFlag())) {
            return null;
        }
        return current;
    }

    private void fillAllowedActions(BidRegistrationVO registrationVO) {
        if (Objects.equals(registrationVO.getStatus(), BidRegistrationStatusEnum.SUBMITTED.getCode())) {
            registrationVO.setAllowedActions(List.of("approve-registration", "reject-registration", "cancel-registration"));
            return;
        }
        if (Objects.equals(registrationVO.getStatus(), BidRegistrationStatusEnum.REJECTED.getCode())) {
            registrationVO.setAllowedActions(List.of("cancel-registration"));
            return;
        }
        registrationVO.setAllowedActions(Collections.emptyList());
    }
}
