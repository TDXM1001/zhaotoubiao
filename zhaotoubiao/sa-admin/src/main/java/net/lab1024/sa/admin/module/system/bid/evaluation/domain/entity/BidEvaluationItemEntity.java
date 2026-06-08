package net.lab1024.sa.admin.module.system.bid.evaluation.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 评标明细实体
 *
 * @author Codex
 * @date 2026-06-08
 */
@Data
@TableName("t_bid_evaluation_item")
public class BidEvaluationItemEntity {

    @TableId(type = IdType.AUTO)
    private Long evaluationItemId;

    private Long evaluationId;

    private Long openingItemId;

    private Long submissionId;

    private String supplierNameSnapshot;

    private BigDecimal quotedPrice;

    private BigDecimal totalScore;

    private Integer rankingNo;

    private Boolean recommendFlag;

    private String evaluationComment;

    private Boolean deletedFlag;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
