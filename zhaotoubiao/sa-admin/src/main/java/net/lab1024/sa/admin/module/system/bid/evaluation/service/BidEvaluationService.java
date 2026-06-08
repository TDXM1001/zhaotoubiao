package net.lab1024.sa.admin.module.system.bid.evaluation.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import net.lab1024.sa.admin.module.system.bid.common.constant.BidEvaluationStatusEnum;
import net.lab1024.sa.admin.module.system.bid.common.constant.BidLotStatusEnum;
import net.lab1024.sa.admin.module.system.bid.common.constant.BidOpeningStatusEnum;
import net.lab1024.sa.admin.module.system.bid.common.constant.BidProjectStatusEnum;
import net.lab1024.sa.admin.module.system.bid.common.service.BidWorkflowHistoryService;
import net.lab1024.sa.admin.module.system.bid.evaluation.dao.BidEvaluationDao;
import net.lab1024.sa.admin.module.system.bid.evaluation.domain.entity.BidEvaluationEntity;
import net.lab1024.sa.admin.module.system.bid.evaluation.domain.form.BidEvaluationActionForm;
import net.lab1024.sa.admin.module.system.bid.evaluation.domain.form.BidEvaluationCreateForm;
import net.lab1024.sa.admin.module.system.bid.evaluation.domain.form.BidEvaluationQueryForm;
import net.lab1024.sa.admin.module.system.bid.evaluation.domain.vo.BidEvaluationVO;
import net.lab1024.sa.admin.module.system.bid.lot.dao.BidLotDao;
import net.lab1024.sa.admin.module.system.bid.lot.domain.entity.BidLotEntity;
import net.lab1024.sa.admin.module.system.bid.opening.dao.BidOpeningDao;
import net.lab1024.sa.admin.module.system.bid.opening.domain.entity.BidOpeningEntity;
import net.lab1024.sa.admin.module.system.bid.project.dao.BidProjectDao;
import net.lab1024.sa.admin.module.system.bid.project.domain.entity.BidProjectEntity;
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
 * 评标 Service
 *
 * @author Codex
 * @date 2026-06-08
 */
@Service
public class BidEvaluationService {

    @Resource
    private BidEvaluationDao bidEvaluationDao;

    @Resource
    private BidProjectDao bidProjectDao;

    @Resource
    private BidLotDao bidLotDao;

    @Resource
    private BidOpeningDao bidOpeningDao;

    @Resource
    private BidWorkflowHistoryService bidWorkflowHistoryService;

    /**
     * 分页查询
     */
    public PageResult<BidEvaluationVO> queryPage(BidEvaluationQueryForm queryForm) {
        queryForm.setDeletedFlag(Boolean.FALSE);
        Page<?> page = SmartPageUtil.convert2PageQuery(queryForm);
        List<BidEvaluationVO> list = bidEvaluationDao.queryPage(page, queryForm);
        if (list != null) {
            list.forEach(this::fillAllowedActions);
        }
        return SmartPageUtil.convert2PageResult(page, list);
    }

    /**
     * 查询详情
     */
    public ResponseDTO<BidEvaluationVO> getDetail(Long evaluationId) {
        BidEvaluationVO detail = bidEvaluationDao.getDetail(evaluationId);
        if (detail == null) {
            return ResponseDTO.error(UserErrorCode.DATA_NOT_EXIST);
        }
        fillDetail(detail);
        return ResponseDTO.ok(detail);
    }

    /**
     * 查询标段评标记录
     */
    public ResponseDTO<BidEvaluationVO> getByLotId(Long lotId) {
        BidEvaluationVO detail = bidEvaluationDao.getByLotId(lotId);
        if (detail == null) {
            return ResponseDTO.error(UserErrorCode.DATA_NOT_EXIST);
        }
        fillDetail(detail);
        return ResponseDTO.ok(detail);
    }

    /**
     * 创建评标记录
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> create(BidEvaluationCreateForm createForm) {
        ResponseDTO<BidOpeningEntity> validateResult = validateCreateForm(createForm);
        if (!validateResult.getOk()) {
            return ResponseDTO.error(validateResult);
        }
        BidOpeningEntity opening = validateResult.getData();

        BidEvaluationEntity entity = SmartBeanUtil.copy(createForm, BidEvaluationEntity.class);
        entity.setOpeningId(opening.getOpeningId());
        if (StringUtils.isBlank(entity.getEvaluationMode())) {
            BidLotEntity lot = bidLotDao.selectById(createForm.getLotId());
            entity.setEvaluationMode(lot == null ? null : lot.getEvaluationMode());
        }
        entity.setStatus(BidEvaluationStatusEnum.PENDING.getCode());
        entity.setDeletedFlag(Boolean.FALSE);
        entity.setVersion(0);
        entity.setCreateUserId(SmartRequestUtil.getRequestUserId());
        entity.setUpdateUserId(SmartRequestUtil.getRequestUserId());
        bidEvaluationDao.insert(entity);

        bidWorkflowHistoryService.recordEvaluationAction(entity.getEvaluationId(), entity.getProjectId(), entity.getLotId(),
                null, entity.getStatus(), "create-evaluation", "创建评标记录", SmartRequestUtil.getRequestUser(), entity);
        return ResponseDTO.ok();
    }

    /**
     * 开始评标
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> start(BidEvaluationActionForm actionForm) {
        BidEvaluationEntity current = getCurrent(actionForm.getEvaluationId());
        if (current == null) {
            return ResponseDTO.error(UserErrorCode.DATA_NOT_EXIST);
        }
        if (!Objects.equals(current.getVersion(), actionForm.getVersion())) {
            return ResponseDTO.userErrorParam("数据已更新，请刷新后重试");
        }
        if (!Objects.equals(current.getStatus(), BidEvaluationStatusEnum.PENDING.getCode())) {
            return ResponseDTO.userErrorParam("只有待评标记录才能开始评标");
        }

        Long itemCount = bidEvaluationDao.countItems(current.getEvaluationId());
        if (itemCount == null || itemCount == 0) {
            bidEvaluationDao.insertItemsFromOpening(current.getEvaluationId(), current.getOpeningId());
            itemCount = bidEvaluationDao.countItems(current.getEvaluationId());
        }
        if (itemCount == null || itemCount == 0) {
            return ResponseDTO.userErrorParam("当前开标记录没有可评标明细");
        }

        BidEvaluationEntity updateEntity = new BidEvaluationEntity();
        updateEntity.setEvaluationId(current.getEvaluationId());
        updateEntity.setVersion(actionForm.getVersion());
        updateEntity.setStatus(BidEvaluationStatusEnum.SCORING.getCode());
        updateEntity.setStartTime(LocalDateTime.now());
        updateEntity.setUpdateUserId(SmartRequestUtil.getRequestUserId());
        Integer updateCount = bidEvaluationDao.updateWithVersion(updateEntity);
        if (updateCount == null || updateCount == 0) {
            return ResponseDTO.userErrorParam("数据已更新，请刷新后重试");
        }
        updateLotStatus(current.getLotId(), BidLotStatusEnum.EVALUATING.getCode());
        updateProjectStatus(current.getProjectId(), BidProjectStatusEnum.EVALUATING.getCode());

        bidWorkflowHistoryService.recordEvaluationAction(current.getEvaluationId(), current.getProjectId(), current.getLotId(),
                current.getStatus(), BidEvaluationStatusEnum.SCORING.getCode(), "start-evaluation", actionForm.getRemark(), SmartRequestUtil.getRequestUser(), updateEntity);
        return ResponseDTO.ok();
    }

    /**
     * 评标定稿
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> finalizeEvaluation(BidEvaluationActionForm actionForm) {
        BidEvaluationEntity current = getCurrent(actionForm.getEvaluationId());
        if (current == null) {
            return ResponseDTO.error(UserErrorCode.DATA_NOT_EXIST);
        }
        if (!Objects.equals(current.getVersion(), actionForm.getVersion())) {
            return ResponseDTO.userErrorParam("数据已更新，请刷新后重试");
        }
        if (!Objects.equals(current.getStatus(), BidEvaluationStatusEnum.SCORING.getCode())
                && !Objects.equals(current.getStatus(), BidEvaluationStatusEnum.SUMMARIZING.getCode())) {
            return ResponseDTO.userErrorParam("只有评分中或汇总中记录才能定稿");
        }

        BidEvaluationEntity updateEntity = new BidEvaluationEntity();
        updateEntity.setEvaluationId(current.getEvaluationId());
        updateEntity.setVersion(actionForm.getVersion());
        updateEntity.setStatus(BidEvaluationStatusEnum.FINALIZED.getCode());
        updateEntity.setFinalizeTime(LocalDateTime.now());
        updateEntity.setFinalSummary(StringUtils.defaultIfBlank(actionForm.getRemark(), current.getFinalSummary()));
        updateEntity.setUpdateUserId(SmartRequestUtil.getRequestUserId());
        Integer updateCount = bidEvaluationDao.updateWithVersion(updateEntity);
        if (updateCount == null || updateCount == 0) {
            return ResponseDTO.userErrorParam("数据已更新，请刷新后重试");
        }

        bidWorkflowHistoryService.recordEvaluationAction(current.getEvaluationId(), current.getProjectId(), current.getLotId(),
                current.getStatus(), BidEvaluationStatusEnum.FINALIZED.getCode(), "finalize-evaluation", actionForm.getRemark(), SmartRequestUtil.getRequestUser(), updateEntity);
        return ResponseDTO.ok();
    }

    /**
     * 回退评标
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> rollback(BidEvaluationActionForm actionForm) {
        BidEvaluationEntity current = getCurrent(actionForm.getEvaluationId());
        if (current == null) {
            return ResponseDTO.error(UserErrorCode.DATA_NOT_EXIST);
        }
        if (!Objects.equals(current.getVersion(), actionForm.getVersion())) {
            return ResponseDTO.userErrorParam("数据已更新，请刷新后重试");
        }
        if (!Objects.equals(current.getStatus(), BidEvaluationStatusEnum.SCORING.getCode())
                && !Objects.equals(current.getStatus(), BidEvaluationStatusEnum.SUMMARIZING.getCode())
                && !Objects.equals(current.getStatus(), BidEvaluationStatusEnum.FINALIZED.getCode())) {
            return ResponseDTO.userErrorParam("当前评标状态不允许回退");
        }
        if (StringUtils.isBlank(actionForm.getRemark())) {
            return ResponseDTO.userErrorParam("回退原因不能为空");
        }

        BidEvaluationEntity updateEntity = new BidEvaluationEntity();
        updateEntity.setEvaluationId(current.getEvaluationId());
        updateEntity.setVersion(actionForm.getVersion());
        updateEntity.setStatus(BidEvaluationStatusEnum.ROLLED_BACK.getCode());
        updateEntity.setRollbackReason(actionForm.getRemark());
        updateEntity.setUpdateUserId(SmartRequestUtil.getRequestUserId());
        Integer updateCount = bidEvaluationDao.updateWithVersion(updateEntity);
        if (updateCount == null || updateCount == 0) {
            return ResponseDTO.userErrorParam("数据已更新，请刷新后重试");
        }

        bidWorkflowHistoryService.recordEvaluationAction(current.getEvaluationId(), current.getProjectId(), current.getLotId(),
                current.getStatus(), BidEvaluationStatusEnum.ROLLED_BACK.getCode(), "rollback-evaluation", actionForm.getRemark(), SmartRequestUtil.getRequestUser(), updateEntity);
        return ResponseDTO.ok();
    }

    private ResponseDTO<BidOpeningEntity> validateCreateForm(BidEvaluationCreateForm form) {
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
        if (!Objects.equals(lot.getStatus(), BidLotStatusEnum.OPENED.getCode())) {
            return ResponseDTO.userErrorParam("只有已开标标段才能创建评标记录");
        }

        BidOpeningEntity opening = form.getOpeningId() == null ? selectCompletedOpeningByLotId(form.getLotId()) : bidOpeningDao.selectById(form.getOpeningId());
        if (opening == null || Boolean.TRUE.equals(opening.getDeletedFlag())) {
            return ResponseDTO.userErrorParam("开标记录不存在");
        }
        if (!Objects.equals(opening.getProjectId(), form.getProjectId()) || !Objects.equals(opening.getLotId(), form.getLotId())) {
            return ResponseDTO.userErrorParam("开标记录与项目或标段不一致");
        }
        if (!Objects.equals(opening.getStatus(), BidOpeningStatusEnum.COMPLETED.getCode())) {
            return ResponseDTO.userErrorParam("只有已完成开标才能创建评标记录");
        }

        Long count = bidEvaluationDao.selectCount(Wrappers.<BidEvaluationEntity>lambdaQuery()
                .eq(BidEvaluationEntity::getLotId, form.getLotId())
                .eq(BidEvaluationEntity::getDeletedFlag, Boolean.FALSE));
        if (count != null && count > 0) {
            return ResponseDTO.userErrorParam("当前标段已存在评标记录");
        }
        return ResponseDTO.ok(opening);
    }

    private BidOpeningEntity selectCompletedOpeningByLotId(Long lotId) {
        return bidOpeningDao.selectOne(Wrappers.<BidOpeningEntity>lambdaQuery()
                .eq(BidOpeningEntity::getLotId, lotId)
                .eq(BidOpeningEntity::getStatus, BidOpeningStatusEnum.COMPLETED.getCode())
                .eq(BidOpeningEntity::getDeletedFlag, Boolean.FALSE)
                .last("LIMIT 1"));
    }

    private BidEvaluationEntity getCurrent(Long evaluationId) {
        BidEvaluationEntity current = bidEvaluationDao.selectById(evaluationId);
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

    private void updateProjectStatus(Long projectId, String status) {
        BidProjectEntity project = bidProjectDao.selectById(projectId);
        if (project == null || Boolean.TRUE.equals(project.getDeletedFlag()) || Objects.equals(project.getStatus(), status)) {
            return;
        }
        BidProjectEntity updateEntity = new BidProjectEntity();
        updateEntity.setProjectId(projectId);
        updateEntity.setVersion(project.getVersion());
        updateEntity.setStatus(status);
        updateEntity.setUpdateUserId(SmartRequestUtil.getRequestUserId());
        bidProjectDao.updateWithVersion(updateEntity);
    }

    private void fillAllowedActions(BidEvaluationVO evaluationVO) {
        if (Objects.equals(evaluationVO.getStatus(), BidEvaluationStatusEnum.PENDING.getCode())) {
            evaluationVO.setAllowedActions(List.of("start-evaluation"));
            return;
        }
        if (Objects.equals(evaluationVO.getStatus(), BidEvaluationStatusEnum.SCORING.getCode())
                || Objects.equals(evaluationVO.getStatus(), BidEvaluationStatusEnum.SUMMARIZING.getCode())) {
            evaluationVO.setAllowedActions(List.of("finalize-evaluation", "rollback-evaluation"));
            return;
        }
        if (Objects.equals(evaluationVO.getStatus(), BidEvaluationStatusEnum.FINALIZED.getCode())) {
            evaluationVO.setAllowedActions(List.of("rollback-evaluation"));
            return;
        }
        evaluationVO.setAllowedActions(Collections.emptyList());
    }

    private void fillDetail(BidEvaluationVO evaluationVO) {
        fillAllowedActions(evaluationVO);
        evaluationVO.setItemList(bidEvaluationDao.listItems(evaluationVO.getEvaluationId()));
    }
}
