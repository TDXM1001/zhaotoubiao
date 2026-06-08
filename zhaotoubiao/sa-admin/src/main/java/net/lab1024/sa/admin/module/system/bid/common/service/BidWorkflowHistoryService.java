package net.lab1024.sa.admin.module.system.bid.common.service;

import com.alibaba.fastjson.JSON;
import jakarta.annotation.Resource;
import net.lab1024.sa.admin.module.system.bid.common.constant.BidBusinessTypeEnum;
import net.lab1024.sa.admin.module.system.bid.common.dao.BidWorkflowHistoryDao;
import net.lab1024.sa.admin.module.system.bid.common.domain.entity.BidWorkflowHistoryEntity;
import net.lab1024.sa.base.common.domain.RequestUser;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 招投标流程历史服务
 *
 * @author Codex
 * @date 2026-06-07
 */
@Service
public class BidWorkflowHistoryService {

    @Resource
    private BidWorkflowHistoryDao bidWorkflowHistoryDao;

    /**
     * 记录项目动作
     */
    public void recordProjectAction(Long projectId,
                                    String fromStatus,
                                    String toStatus,
                                    String actionCode,
                                    String operateComment,
                                    RequestUser requestUser,
                                    Object snapshot) {
        this.record(BidBusinessTypeEnum.PROJECT, projectId, projectId, null, fromStatus, toStatus, actionCode, operateComment, requestUser, snapshot);
    }

    /**
     * 记录标段动作
     */
    public void recordLotAction(Long lotId,
                                Long projectId,
                                String fromStatus,
                                String toStatus,
                                String actionCode,
                                String operateComment,
                                RequestUser requestUser,
                                Object snapshot) {
        this.record(BidBusinessTypeEnum.LOT, lotId, projectId, lotId, fromStatus, toStatus, actionCode, operateComment, requestUser, snapshot);
    }

    /**
     * 记录报名动作
     */
    public void recordRegistrationAction(Long registrationId,
                                         Long projectId,
                                         Long lotId,
                                         String fromStatus,
                                         String toStatus,
                                         String actionCode,
                                         String operateComment,
                                         RequestUser requestUser,
                                         Object snapshot) {
        this.record(BidBusinessTypeEnum.REGISTRATION, registrationId, projectId, lotId, fromStatus, toStatus, actionCode, operateComment, requestUser, snapshot);
    }

    /**
     * 记录招标文件版本动作
     */
    public void recordTenderAction(Long tenderVersionId,
                                   Long projectId,
                                   Long lotId,
                                   String fromStatus,
                                   String toStatus,
                                   String actionCode,
                                   String operateComment,
                                   RequestUser requestUser,
                                   Object snapshot) {
        this.record(BidBusinessTypeEnum.TENDER_VERSION, tenderVersionId, projectId, lotId, fromStatus, toStatus, actionCode, operateComment, requestUser, snapshot);
    }

    /**
     * 记录投标动作
     */
    public void recordSubmissionAction(Long submissionId,
                                       Long projectId,
                                       Long lotId,
                                       String fromStatus,
                                       String toStatus,
                                       String actionCode,
                                       String operateComment,
                                       RequestUser requestUser,
                                       Object snapshot) {
        this.record(BidBusinessTypeEnum.SUBMISSION, submissionId, projectId, lotId, fromStatus, toStatus, actionCode, operateComment, requestUser, snapshot);
    }

    private void record(BidBusinessTypeEnum businessType,
                        Long businessId,
                        Long projectId,
                        Long lotId,
                        String fromStatus,
                        String toStatus,
                        String actionCode,
                        String operateComment,
                        RequestUser requestUser,
                        Object snapshot) {
        BidWorkflowHistoryEntity historyEntity = new BidWorkflowHistoryEntity();
        historyEntity.setBusinessType(businessType.getCode());
        historyEntity.setBusinessId(businessId);
        historyEntity.setProjectId(projectId);
        historyEntity.setLotId(lotId);
        historyEntity.setFromStatus(fromStatus);
        historyEntity.setToStatus(toStatus);
        historyEntity.setActionCode(actionCode);
        historyEntity.setOperateComment(operateComment);
        historyEntity.setOperateTime(LocalDateTime.now());
        if (requestUser != null) {
            historyEntity.setOperatorId(requestUser.getUserId());
            historyEntity.setOperatorName(requestUser.getUserName());
            historyEntity.setOperatorSide(requestUser.getUserType() == null ? null : requestUser.getUserType().name());
        }
        if (snapshot != null) {
            historyEntity.setSnapshotJson(JSON.toJSONString(snapshot));
        }
        bidWorkflowHistoryDao.insert(historyEntity);
    }
}
