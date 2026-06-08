package net.lab1024.sa.admin.module.system.bid.evaluation.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 评标主记录实体
 *
 * @author Codex
 * @date 2026-06-08
 */
@Data
@TableName("t_bid_evaluation")
public class BidEvaluationEntity {

    @TableId(type = IdType.AUTO)
    private Long evaluationId;

    private Long projectId;

    private Long lotId;

    private Long openingId;

    private String status;

    private String evaluationMode;

    private LocalDateTime startTime;

    private LocalDateTime finalizeTime;

    private String finalSummary;

    private String rollbackReason;

    private Boolean deletedFlag;

    private Integer version;

    private Long createUserId;

    private LocalDateTime createTime;

    private Long updateUserId;

    private LocalDateTime updateTime;
}
