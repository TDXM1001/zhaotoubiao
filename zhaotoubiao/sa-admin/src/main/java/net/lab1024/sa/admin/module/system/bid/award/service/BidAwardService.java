package net.lab1024.sa.admin.module.system.bid.award.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import net.lab1024.sa.admin.module.system.bid.award.dao.BidAwardDao;
import net.lab1024.sa.admin.module.system.bid.award.domain.entity.BidAwardEntity;
import net.lab1024.sa.admin.module.system.bid.award.domain.form.BidAwardActionForm;
import net.lab1024.sa.admin.module.system.bid.award.domain.form.BidAwardCreateForm;
import net.lab1024.sa.admin.module.system.bid.award.domain.form.BidAwardQueryForm;
import net.lab1024.sa.admin.module.system.bid.award.domain.vo.BidAwardVO;
import net.lab1024.sa.admin.module.system.bid.common.constant.BidAwardStatusEnum;
import net.lab1024.sa.admin.module.system.bid.common.constant.BidEvaluationStatusEnum;
import net.lab1024.sa.admin.module.system.bid.common.constant.BidLotStatusEnum;
import net.lab1024.sa.admin.module.system.bid.common.constant.BidProjectStatusEnum;
import net.lab1024.sa.admin.module.system.bid.common.constant.BidSubmissionStatusEnum;
import net.lab1024.sa.admin.module.system.bid.common.service.BidWorkflowHistoryService;
import net.lab1024.sa.admin.module.system.bid.evaluation.dao.BidEvaluationDao;
import net.lab1024.sa.admin.module.system.bid.evaluation.domain.entity.BidEvaluationEntity;
import net.lab1024.sa.admin.module.system.bid.lot.dao.BidLotDao;
import net.lab1024.sa.admin.module.system.bid.lot.domain.entity.BidLotEntity;
import net.lab1024.sa.admin.module.system.bid.project.dao.BidProjectDao;
import net.lab1024.sa.admin.module.system.bid.project.domain.entity.BidProjectEntity;
import net.lab1024.sa.admin.module.system.bid.submission.dao.BidSubmissionDao;
import net.lab1024.sa.admin.module.system.bid.submission.domain.entity.BidSubmissionEntity;
import net.lab1024.sa.base.common.code.UserErrorCode;
import net.lab1024.sa.base.common.domain.PageResult;
import net.lab1024.sa.base.common.domain.ResponseDTO;
import net.lab1024.sa.base.common.util.SmartPageUtil;
import net.lab1024.sa.base.common.util.SmartRequestUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 定标 Service
 *
 * @author Codex
 * @date 2026-06-08
 */
@Service
public class BidAwardService {

    @Resource
    private BidAwardDao bidAwardDao;

    @Resource
    private BidProjectDao bidProjectDao;

    @Resource
    private BidLotDao bidLotDao;

    @Resource
    private BidEvaluationDao bidEvaluationDao;

    @Resource
    private BidSubmissionDao bidSubmissionDao;

    @Resource
    private BidWorkflowHistoryService bidWorkflowHistoryService;

    /**
     * 分页查询
     */
    public PageResult<BidAwardVO> queryPage(BidAwardQueryForm queryForm) {
        queryForm.setDeletedFlag(Boolean.FALSE);
        Page<?> page = SmartPageUtil.convert2PageQuery(queryForm);
        List<BidAwardVO> list = bidAwardDao.queryPage(page, queryForm);
        if (list != null) {
            list.forEach(this::fillAllowedActions);
        }
        return SmartPageUtil.convert2PageResult(page, list);
    }

    /**
     * 查询详情
     */
    public ResponseDTO<BidAwardVO> getDetail(Long awardId) {
        BidAwardVO detail = bidAwardDao.getDetail(awardId);
        if (detail == null) {
            return ResponseDTO.error(UserErrorCode.DATA_NOT_EXIST);
        }
        fillAllowedActions(detail);
        return ResponseDTO.ok(detail);
    }

    /**
     * 查询标段定标记录
     */
    public ResponseDTO<BidAwardVO> getByLotId(Long lotId) {
        BidAwardVO detail = bidAwardDao.getByLotId(lotId);
        if (detail == null) {
            return ResponseDTO.error(UserErrorCode.DATA_NOT_EXIST);
        }
        fillAllowedActions(detail);
        return ResponseDTO.ok(detail);
    }

    /**
     * 创建定标记录
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> create(BidAwardCreateForm createForm) {
        BidEvaluationEntity evaluation = bidEvaluationDao.selectById(createForm.getEvaluationId());
        ResponseDTO<String> validateResult = validateCreateForm(createForm, evaluation);
        if (!validateResult.getOk()) {
            return validateResult;
        }

        BidSubmissionEntity submission = bidSubmissionDao.selectById(createForm.getWinningSubmissionId());
        BidAwardEntity entity = new BidAwardEntity();
        entity.setProjectId(createForm.getProjectId());
        entity.setLotId(createForm.getLotId());
        entity.setEvaluationId(createForm.getEvaluationId());
        entity.setStatus(BidAwardStatusEnum.PENDING.getCode());
        entity.setWinningSubmissionId(createForm.getWinningSubmissionId());
        entity.setWinnerEnterpriseId(submission.getSupplierEnterpriseId());
        entity.setWinnerNameSnapshot(submission.getSupplierNameSnapshot());
        entity.setWinnerCreditCode(submission.getSupplierCreditCode());
        entity.setAwardAmount(createForm.getAwardAmount() == null ? bidAwardDao.getSubmissionLatestPrice(createForm.getWinningSubmissionId()) : createForm.getAwardAmount());
        entity.setRemark(createForm.getRemark());
        entity.setDeletedFlag(Boolean.FALSE);
        entity.setVersion(0);
        entity.setCreateUserId(SmartRequestUtil.getRequestUserId());
        entity.setUpdateUserId(SmartRequestUtil.getRequestUserId());
        bidAwardDao.insert(entity);

        bidWorkflowHistoryService.recordAwardAction(entity.getAwardId(), entity.getProjectId(), entity.getLotId(),
                null, entity.getStatus(), "create-award", "创建定标记录", SmartRequestUtil.getRequestUser(), entity);
        return ResponseDTO.ok();
    }

    /**
     * 确认定标
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> confirm(BidAwardActionForm actionForm) {
        BidAwardEntity current = getCurrent(actionForm.getAwardId());
        if (current == null) {
            return ResponseDTO.error(UserErrorCode.DATA_NOT_EXIST);
        }
        if (!Objects.equals(current.getVersion(), actionForm.getVersion())) {
            return ResponseDTO.userErrorParam("数据已更新，请刷新后重试");
        }
        if (!Objects.equals(current.getStatus(), BidAwardStatusEnum.PENDING.getCode())
                && !Objects.equals(current.getStatus(), BidAwardStatusEnum.REVIEWING.getCode())) {
            return ResponseDTO.userErrorParam("只有待定标或确认中记录才能确认定标");
        }

        BidAwardEntity updateEntity = new BidAwardEntity();
        updateEntity.setAwardId(current.getAwardId());
        updateEntity.setVersion(actionForm.getVersion());
        updateEntity.setStatus(BidAwardStatusEnum.CONFIRMED.getCode());
        updateEntity.setConfirmUserId(SmartRequestUtil.getRequestUserId());
        updateEntity.setConfirmTime(LocalDateTime.now());
        updateEntity.setPublicNoticeTime(LocalDateTime.now());
        updateEntity.setRemark(StringUtils.defaultIfBlank(actionForm.getRemark(), current.getRemark()));
        updateEntity.setUpdateUserId(SmartRequestUtil.getRequestUserId());
        Integer updateCount = bidAwardDao.updateWithVersion(updateEntity);
        if (updateCount == null || updateCount == 0) {
            return ResponseDTO.userErrorParam("数据已更新，请刷新后重试");
        }

        updateLotStatus(current.getLotId(), BidLotStatusEnum.AWARDED.getCode());
        updateProjectAwardedIfAllLotsAwarded(current.getProjectId());

        bidWorkflowHistoryService.recordAwardAction(current.getAwardId(), current.getProjectId(), current.getLotId(),
                current.getStatus(), BidAwardStatusEnum.CONFIRMED.getCode(), "confirm-award", actionForm.getRemark(), SmartRequestUtil.getRequestUser(), updateEntity);
        return ResponseDTO.ok();
    }

    /**
     * 回退定标
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> rollback(BidAwardActionForm actionForm) {
        BidAwardEntity current = getCurrent(actionForm.getAwardId());
        if (current == null) {
            return ResponseDTO.error(UserErrorCode.DATA_NOT_EXIST);
        }
        if (!Objects.equals(current.getVersion(), actionForm.getVersion())) {
            return ResponseDTO.userErrorParam("数据已更新，请刷新后重试");
        }
        if (!Objects.equals(current.getStatus(), BidAwardStatusEnum.CONFIRMED.getCode())) {
            return ResponseDTO.userErrorParam("只有已确认定标才能回退");
        }
        if (StringUtils.isBlank(actionForm.getRemark())) {
            return ResponseDTO.userErrorParam("回退原因不能为空");
        }

        BidAwardEntity updateEntity = new BidAwardEntity();
        updateEntity.setAwardId(current.getAwardId());
        updateEntity.setVersion(actionForm.getVersion());
        updateEntity.setStatus(BidAwardStatusEnum.ROLLED_BACK.getCode());
        updateEntity.setRollbackReason(actionForm.getRemark());
        updateEntity.setUpdateUserId(SmartRequestUtil.getRequestUserId());
        Integer updateCount = bidAwardDao.updateWithVersion(updateEntity);
        if (updateCount == null || updateCount == 0) {
            return ResponseDTO.userErrorParam("数据已更新，请刷新后重试");
        }

        bidWorkflowHistoryService.recordAwardAction(current.getAwardId(), current.getProjectId(), current.getLotId(),
                current.getStatus(), BidAwardStatusEnum.ROLLED_BACK.getCode(), "rollback-award", actionForm.getRemark(), SmartRequestUtil.getRequestUser(), updateEntity);
        return ResponseDTO.ok();
    }

    /**
     * 取消定标
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> cancel(BidAwardActionForm actionForm) {
        BidAwardEntity current = getCurrent(actionForm.getAwardId());
        if (current == null) {
            return ResponseDTO.error(UserErrorCode.DATA_NOT_EXIST);
        }
        if (!Objects.equals(current.getVersion(), actionForm.getVersion())) {
            return ResponseDTO.userErrorParam("数据已更新，请刷新后重试");
        }
        if (!Objects.equals(current.getStatus(), BidAwardStatusEnum.PENDING.getCode())
                && !Objects.equals(current.getStatus(), BidAwardStatusEnum.REVIEWING.getCode())) {
            return ResponseDTO.userErrorParam("只有待定标或确认中记录才能取消");
        }

        BidAwardEntity updateEntity = new BidAwardEntity();
        updateEntity.setAwardId(current.getAwardId());
        updateEntity.setVersion(actionForm.getVersion());
        updateEntity.setStatus(BidAwardStatusEnum.CANCELLED.getCode());
        updateEntity.setRemark(StringUtils.defaultIfBlank(actionForm.getRemark(), current.getRemark()));
        updateEntity.setUpdateUserId(SmartRequestUtil.getRequestUserId());
        Integer updateCount = bidAwardDao.updateWithVersion(updateEntity);
        if (updateCount == null || updateCount == 0) {
            return ResponseDTO.userErrorParam("数据已更新，请刷新后重试");
        }

        bidWorkflowHistoryService.recordAwardAction(current.getAwardId(), current.getProjectId(), current.getLotId(),
                current.getStatus(), BidAwardStatusEnum.CANCELLED.getCode(), "cancel-award", actionForm.getRemark(), SmartRequestUtil.getRequestUser(), updateEntity);
        return ResponseDTO.ok();
    }

    private ResponseDTO<String> validateCreateForm(BidAwardCreateForm form, BidEvaluationEntity evaluation) {
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

        if (evaluation == null || Boolean.TRUE.equals(evaluation.getDeletedFlag())) {
            return ResponseDTO.userErrorParam("评标记录不存在");
        }
        if (!Objects.equals(evaluation.getProjectId(), form.getProjectId()) || !Objects.equals(evaluation.getLotId(), form.getLotId())) {
            return ResponseDTO.userErrorParam("评标记录与项目或标段不一致");
        }
        if (!Objects.equals(evaluation.getStatus(), BidEvaluationStatusEnum.FINALIZED.getCode())) {
            return ResponseDTO.userErrorParam("只有已定稿评标才能创建定标记录");
        }

        BidSubmissionEntity submission = bidSubmissionDao.selectById(form.getWinningSubmissionId());
        if (submission == null || Boolean.TRUE.equals(submission.getDeletedFlag())) {
            return ResponseDTO.userErrorParam("中标投标记录不存在");
        }
        if (!Objects.equals(submission.getProjectId(), form.getProjectId()) || !Objects.equals(submission.getLotId(), form.getLotId())) {
            return ResponseDTO.userErrorParam("中标投标记录与项目或标段不一致");
        }
        if (!Objects.equals(submission.getStatus(), BidSubmissionStatusEnum.OPENED.getCode())) {
            return ResponseDTO.userErrorParam("只有已开标投标才能参与定标");
        }

        Long count = bidAwardDao.selectCount(Wrappers.<BidAwardEntity>lambdaQuery()
                .eq(BidAwardEntity::getLotId, form.getLotId())
                .eq(BidAwardEntity::getDeletedFlag, Boolean.FALSE));
        if (count != null && count > 0) {
            return ResponseDTO.userErrorParam("当前标段已存在定标记录");
        }
        return ResponseDTO.ok();
    }

    private BidAwardEntity getCurrent(Long awardId) {
        BidAwardEntity current = bidAwardDao.selectById(awardId);
        if (current == null || Boolean.TRUE.equals(current.getDeletedFlag())) {
            return null;
        }
        return current;
    }

    private void updateLotStatus(Long lotId, String status) {
        BidLotEntity lot = bidLotDao.selectById(lotId);
        if (lot == null || Boolean.TRUE.equals(lot.getDeletedFlag()) || Objects.equals(lot.getStatus(), status)) {
            return;
        }
        BidLotEntity updateEntity = new BidLotEntity();
        updateEntity.setLotId(lotId);
        updateEntity.setVersion(lot.getVersion());
        updateEntity.setStatus(status);
        updateEntity.setUpdateUserId(SmartRequestUtil.getRequestUserId());
        bidLotDao.updateWithVersion(updateEntity);
    }

    private void updateProjectAwardedIfAllLotsAwarded(Long projectId) {
        Long notAwardedCount = bidLotDao.selectCount(Wrappers.<BidLotEntity>lambdaQuery()
                .eq(BidLotEntity::getProjectId, projectId)
                .eq(BidLotEntity::getDeletedFlag, Boolean.FALSE)
                .ne(BidLotEntity::getStatus, BidLotStatusEnum.VOIDED.getCode())
                .ne(BidLotEntity::getStatus, BidLotStatusEnum.AWARDED.getCode()));
        if (notAwardedCount != null && notAwardedCount > 0) {
            return;
        }
        BidProjectEntity project = bidProjectDao.selectById(projectId);
        if (project == null || Boolean.TRUE.equals(project.getDeletedFlag()) || Objects.equals(project.getStatus(), BidProjectStatusEnum.AWARDED.getCode())) {
            return;
        }
        BidProjectEntity updateEntity = new BidProjectEntity();
        updateEntity.setProjectId(projectId);
        updateEntity.setVersion(project.getVersion());
        updateEntity.setStatus(BidProjectStatusEnum.AWARDED.getCode());
        updateEntity.setUpdateUserId(SmartRequestUtil.getRequestUserId());
        bidProjectDao.updateWithVersion(updateEntity);
    }

    private void fillAllowedActions(BidAwardVO awardVO) {
        if (Objects.equals(awardVO.getStatus(), BidAwardStatusEnum.PENDING.getCode())
                || Objects.equals(awardVO.getStatus(), BidAwardStatusEnum.REVIEWING.getCode())) {
            awardVO.setAllowedActions(List.of("confirm-award", "cancel-award"));
            return;
        }
        if (Objects.equals(awardVO.getStatus(), BidAwardStatusEnum.CONFIRMED.getCode())) {
            awardVO.setAllowedActions(List.of("rollback-award"));
            return;
        }
        awardVO.setAllowedActions(Collections.emptyList());
    }
}
