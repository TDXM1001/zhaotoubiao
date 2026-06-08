package net.lab1024.sa.admin.module.system.bid.evaluation.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 评标明细视图对象
 *
 * @author Codex
 * @date 2026-06-08
 */
@Data
public class BidEvaluationItemVO {

    @Schema(description = "评标明细ID")
    private Long evaluationItemId;

    @Schema(description = "评标ID")
    private Long evaluationId;

    @Schema(description = "开标明细ID")
    private Long openingItemId;

    @Schema(description = "投标ID")
    private Long submissionId;

    @Schema(description = "供应商名称快照")
    private String supplierNameSnapshot;

    @Schema(description = "报价金额")
    private BigDecimal quotedPrice;

    @Schema(description = "总分")
    private BigDecimal totalScore;

    @Schema(description = "排名")
    private Integer rankingNo;

    @Schema(description = "是否推荐")
    private Boolean recommendFlag;

    @Schema(description = "评标意见")
    private String evaluationComment;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
