package net.lab1024.sa.admin.module.system.bid.award.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 定标主记录实体
 *
 * @author Codex
 * @date 2026-06-08
 */
@Data
@TableName("t_bid_award")
public class BidAwardEntity {

    @TableId(type = IdType.AUTO)
    private Long awardId;

    private Long projectId;

    private Long lotId;

    private Long evaluationId;

    private String status;

    private Long winningSubmissionId;

    private Long winnerEnterpriseId;

    private String winnerNameSnapshot;

    private String winnerCreditCode;

    private BigDecimal awardAmount;

    private Long confirmUserId;

    private LocalDateTime confirmTime;

    private LocalDateTime publicNoticeTime;

    private String rollbackReason;

    private String remark;

    private Boolean deletedFlag;

    private Integer version;

    private Long createUserId;

    private LocalDateTime createTime;

    private Long updateUserId;

    private LocalDateTime updateTime;
}
