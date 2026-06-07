package net.lab1024.sa.admin.module.system.bid.common.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 招投标流程历史
 *
 * @author Codex
 * @date 2026-06-07
 */
@Data
@TableName("t_bid_workflow_history")
public class BidWorkflowHistoryEntity {

    @TableId(type = IdType.AUTO)
    private Long historyId;

    private String businessType;

    private Long businessId;

    private Long projectId;

    private Long lotId;

    private String fromStatus;

    private String toStatus;

    private String actionCode;

    private Long operatorId;

    private String operatorName;

    private String operatorSide;

    private String operateComment;

    private String snapshotJson;

    private LocalDateTime operateTime;

    private LocalDateTime createTime;
}
