package net.lab1024.sa.admin.module.system.bid.lot.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import net.lab1024.sa.admin.module.system.bid.common.constant.BidLotStatusEnum;
import net.lab1024.sa.admin.module.system.bid.common.constant.BidProjectStatusEnum;
import net.lab1024.sa.admin.module.system.bid.common.service.BidWorkflowHistoryService;
import net.lab1024.sa.admin.module.system.bid.lot.dao.BidLotDao;
import net.lab1024.sa.admin.module.system.bid.lot.domain.entity.BidLotEntity;
import net.lab1024.sa.admin.module.system.bid.lot.domain.form.BidLotActionForm;
import net.lab1024.sa.admin.module.system.bid.lot.domain.form.BidLotAddForm;
import net.lab1024.sa.admin.module.system.bid.lot.domain.form.BidLotQueryForm;
import net.lab1024.sa.admin.module.system.bid.lot.domain.form.BidLotUpdateForm;
import net.lab1024.sa.admin.module.system.bid.lot.domain.vo.BidLotVO;
import net.lab1024.sa.admin.module.system.bid.project.dao.BidProjectDao;
import net.lab1024.sa.admin.module.system.bid.project.domain.entity.BidProjectEntity;
import net.lab1024.sa.base.common.code.UserErrorCode;
import net.lab1024.sa.base.common.domain.PageResult;
import net.lab1024.sa.base.common.domain.ResponseDTO;
import net.lab1024.sa.base.common.util.SmartBeanUtil;
import net.lab1024.sa.base.common.util.SmartPageUtil;
import net.lab1024.sa.base.common.util.SmartRequestUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 标段 Service
 *
 * @author Codex
 * @date 2026-06-07
 */
@Service
public class BidLotService {

    @Resource
    private BidLotDao bidLotDao;

    @Resource
    private BidProjectDao bidProjectDao;

    @Resource
    private BidWorkflowHistoryService bidWorkflowHistoryService;

    /**
     * 分页查询
     */
    public PageResult<BidLotVO> queryPage(BidLotQueryForm queryForm) {
        queryForm.setDeletedFlag(Boolean.FALSE);
        Page<?> page = SmartPageUtil.convert2PageQuery(queryForm);
        List<BidLotVO> list = bidLotDao.queryPage(page, queryForm);
        if (list != null) {
            list.forEach(this::fillAllowedActions);
        }
        return SmartPageUtil.convert2PageResult(page, list);
    }

    /**
     * 查询详情
     */
    public ResponseDTO<BidLotVO> getDetail(Long lotId) {
        BidLotVO detail = bidLotDao.getDetail(lotId);
        if (detail == null) {
            return ResponseDTO.error(UserErrorCode.DATA_NOT_EXIST);
        }
        fillAllowedActions(detail);
        return ResponseDTO.ok(detail);
    }

    /**
     * 查询项目下标段
     */
    public ResponseDTO<List<BidLotVO>> queryByProjectId(Long projectId) {
        if (projectId == null) {
            return ResponseDTO.ok(Collections.emptyList());
        }
        List<BidLotVO> list = bidLotDao.queryByProjectId(projectId);
        if (list != null) {
            list.forEach(this::fillAllowedActions);
        }
        return ResponseDTO.ok(list);
    }

    /**
     * 新增标段
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> add(BidLotAddForm addForm) {
        BidProjectEntity projectEntity = validateProject(addForm.getProjectId());
        if (projectEntity == null) {
            return ResponseDTO.userErrorParam("所属项目不存在");
        }
        if (!Objects.equals(projectEntity.getStatus(), BidProjectStatusEnum.DRAFT.getCode())
                && !Objects.equals(projectEntity.getStatus(), BidProjectStatusEnum.PLANNED.getCode())) {
            return ResponseDTO.userErrorParam("当前项目状态不允许新增标段");
        }

        ResponseDTO<String> validateResult = validateLotForm(addForm, null);
        if (!validateResult.getOk()) {
            return validateResult;
        }
        if (existsLotCode(addForm.getProjectId(), addForm.getLotCode(), null)) {
            return ResponseDTO.userErrorParam("同一项目下标段编号已存在");
        }
        if (existsLotNo(addForm.getProjectId(), addForm.getLotNo(), null)) {
            return ResponseDTO.userErrorParam("同一项目下标段序号已存在");
        }

        BidLotEntity entity = SmartBeanUtil.copy(addForm, BidLotEntity.class);
        entity.setStatus(BidLotStatusEnum.DRAFT.getCode());
        entity.setDeletedFlag(Boolean.FALSE);
        entity.setVersion(0);
        entity.setCreateUserId(SmartRequestUtil.getRequestUserId());
        entity.setUpdateUserId(SmartRequestUtil.getRequestUserId());
        bidLotDao.insert(entity);

        bidWorkflowHistoryService.recordLotAction(entity.getLotId(), entity.getProjectId(), null, entity.getStatus(), "create-lot", "新建标段", SmartRequestUtil.getRequestUser(), entity);
        return ResponseDTO.ok();
    }

    /**
     * 编辑标段
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> update(BidLotUpdateForm updateForm) {
        BidLotEntity current = bidLotDao.selectById(updateForm.getLotId());
        if (current == null || Boolean.TRUE.equals(current.getDeletedFlag())) {
            return ResponseDTO.error(UserErrorCode.DATA_NOT_EXIST);
        }
        if (!Objects.equals(current.getVersion(), updateForm.getVersion())) {
            return ResponseDTO.userErrorParam("数据已更新，请刷新后重试");
        }
        if (!Objects.equals(current.getStatus(), BidLotStatusEnum.DRAFT.getCode())) {
            return ResponseDTO.userErrorParam("只有草稿标段才能编辑");
        }

        BidProjectEntity projectEntity = validateProject(updateForm.getProjectId());
        if (projectEntity == null) {
            return ResponseDTO.userErrorParam("所属项目不存在");
        }
        ResponseDTO<String> validateResult = validateLotForm(updateForm, updateForm.getLotId());
        if (!validateResult.getOk()) {
            return validateResult;
        }
        if (existsLotCode(updateForm.getProjectId(), updateForm.getLotCode(), updateForm.getLotId())) {
            return ResponseDTO.userErrorParam("同一项目下标段编号已存在");
        }
        if (existsLotNo(updateForm.getProjectId(), updateForm.getLotNo(), updateForm.getLotId())) {
            return ResponseDTO.userErrorParam("同一项目下标段序号已存在");
        }

        BidLotEntity updateEntity = SmartBeanUtil.copy(updateForm, BidLotEntity.class);
        updateEntity.setUpdateUserId(SmartRequestUtil.getRequestUserId());
        Integer updateCount = bidLotDao.updateWithVersion(updateEntity);
        if (updateCount == null || updateCount == 0) {
            return ResponseDTO.userErrorParam("数据已更新，请刷新后重试");
        }

        bidWorkflowHistoryService.recordLotAction(current.getLotId(), current.getProjectId(), current.getStatus(), current.getStatus(), "edit-lot", "编辑标段", SmartRequestUtil.getRequestUser(), updateEntity);
        return ResponseDTO.ok();
    }

    /**
     * 关闭投标
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> closeBid(BidLotActionForm actionForm) {
        BidLotEntity current = bidLotDao.selectById(actionForm.getLotId());
        if (current == null || Boolean.TRUE.equals(current.getDeletedFlag())) {
            return ResponseDTO.error(UserErrorCode.DATA_NOT_EXIST);
        }
        if (!Objects.equals(current.getVersion(), actionForm.getVersion())) {
            return ResponseDTO.userErrorParam("数据已更新，请刷新后重试");
        }
        if (!Objects.equals(current.getStatus(), BidLotStatusEnum.BIDDING.getCode())) {
            return ResponseDTO.userErrorParam("只有投标中的标段才能关闭投标");
        }

        BidLotEntity updateEntity = new BidLotEntity();
        updateEntity.setLotId(current.getLotId());
        updateEntity.setVersion(actionForm.getVersion());
        updateEntity.setStatus(BidLotStatusEnum.BID_CLOSED.getCode());
        updateEntity.setUpdateUserId(SmartRequestUtil.getRequestUserId());
        Integer updateCount = bidLotDao.updateWithVersion(updateEntity);
        if (updateCount == null || updateCount == 0) {
            return ResponseDTO.userErrorParam("数据已更新，请刷新后重试");
        }

        bidWorkflowHistoryService.recordLotAction(current.getLotId(), current.getProjectId(), current.getStatus(), BidLotStatusEnum.BID_CLOSED.getCode(), "close-bid", actionForm.getRemark(), SmartRequestUtil.getRequestUser(), updateEntity);
        return ResponseDTO.ok();
    }

    /**
     * 废止标段
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> voidLot(BidLotActionForm actionForm) {
        BidLotEntity current = bidLotDao.selectById(actionForm.getLotId());
        if (current == null || Boolean.TRUE.equals(current.getDeletedFlag())) {
            return ResponseDTO.error(UserErrorCode.DATA_NOT_EXIST);
        }
        if (!Objects.equals(current.getVersion(), actionForm.getVersion())) {
            return ResponseDTO.userErrorParam("数据已更新，请刷新后重试");
        }
        if (Objects.equals(current.getStatus(), BidLotStatusEnum.VOIDED.getCode())) {
            return ResponseDTO.userErrorParam("当前标段已经废止");
        }

        BidLotEntity updateEntity = new BidLotEntity();
        updateEntity.setLotId(current.getLotId());
        updateEntity.setVersion(actionForm.getVersion());
        updateEntity.setStatus(BidLotStatusEnum.VOIDED.getCode());
        updateEntity.setUpdateUserId(SmartRequestUtil.getRequestUserId());
        Integer updateCount = bidLotDao.updateWithVersion(updateEntity);
        if (updateCount == null || updateCount == 0) {
            return ResponseDTO.userErrorParam("数据已更新，请刷新后重试");
        }

        bidWorkflowHistoryService.recordLotAction(current.getLotId(), current.getProjectId(), current.getStatus(), BidLotStatusEnum.VOIDED.getCode(), "void-lot", actionForm.getRemark(), SmartRequestUtil.getRequestUser(), updateEntity);
        return ResponseDTO.ok();
    }

    private BidProjectEntity validateProject(Long projectId) {
        BidProjectEntity projectEntity = bidProjectDao.selectById(projectId);
        if (projectEntity == null || Boolean.TRUE.equals(projectEntity.getDeletedFlag())) {
            return null;
        }
        return projectEntity;
    }

    private ResponseDTO<String> validateLotForm(BidLotAddForm form, Long lotId) {
        if (form.getRegistrationStartTime() != null && form.getRegistrationEndTime() != null
                && form.getRegistrationStartTime().isAfter(form.getRegistrationEndTime())) {
            return ResponseDTO.userErrorParam("报名开始时间不能晚于报名截止时间");
        }
        if (form.getRegistrationEndTime() != null && form.getBidEndTime() != null
                && form.getRegistrationEndTime().isAfter(form.getBidEndTime())) {
            return ResponseDTO.userErrorParam("报名截止时间不能晚于投标截止时间");
        }
        if (form.getBidStartTime() != null && form.getBidEndTime() != null
                && form.getBidStartTime().isAfter(form.getBidEndTime())) {
            return ResponseDTO.userErrorParam("投标开始时间不能晚于投标截止时间");
        }
        if (form.getBidEndTime() != null && form.getOpeningTime() != null
                && form.getBidEndTime().isAfter(form.getOpeningTime())) {
            return ResponseDTO.userErrorParam("投标截止时间不能晚于开标时间");
        }
        return ResponseDTO.ok();
    }

    private boolean existsLotCode(Long projectId, String lotCode, Long excludeLotId) {
        Long count = bidLotDao.selectCount(Wrappers.<BidLotEntity>lambdaQuery()
                .eq(BidLotEntity::getProjectId, projectId)
                .eq(BidLotEntity::getLotCode, lotCode)
                .eq(BidLotEntity::getDeletedFlag, Boolean.FALSE)
                .ne(excludeLotId != null, BidLotEntity::getLotId, excludeLotId));
        return count != null && count > 0;
    }

    private boolean existsLotNo(Long projectId, Integer lotNo, Long excludeLotId) {
        Long count = bidLotDao.selectCount(Wrappers.<BidLotEntity>lambdaQuery()
                .eq(BidLotEntity::getProjectId, projectId)
                .eq(BidLotEntity::getLotNo, lotNo)
                .eq(BidLotEntity::getDeletedFlag, Boolean.FALSE)
                .ne(excludeLotId != null, BidLotEntity::getLotId, excludeLotId));
        return count != null && count > 0;
    }

    private void fillAllowedActions(BidLotVO lotVO) {
        if (Objects.equals(lotVO.getStatus(), BidLotStatusEnum.DRAFT.getCode())) {
            lotVO.setAllowedActions(List.of("edit-lot", "void-lot"));
            return;
        }
        if (Objects.equals(lotVO.getStatus(), BidLotStatusEnum.BIDDING.getCode())) {
            lotVO.setAllowedActions(List.of("close-bid", "void-lot"));
            return;
        }
        lotVO.setAllowedActions(Collections.emptyList());
    }
}
