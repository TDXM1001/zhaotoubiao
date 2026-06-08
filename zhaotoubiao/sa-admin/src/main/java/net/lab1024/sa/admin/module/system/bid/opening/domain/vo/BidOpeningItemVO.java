package net.lab1024.sa.admin.module.system.bid.opening.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 开标明细视图对象
 *
 * @author Codex
 * @date 2026-06-08
 */
@Data
public class BidOpeningItemVO {

    @Schema(description = "开标明细ID")
    private Long openingItemId;

    @Schema(description = "开标ID")
    private Long openingId;

    @Schema(description = "投标ID")
    private Long submissionId;

    @Schema(description = "投标版本ID")
    private Long submissionVersionId;

    @Schema(description = "供应商企业ID")
    private Long supplierEnterpriseId;

    @Schema(description = "供应商名称快照")
    private String supplierNameSnapshot;

    @Schema(description = "统一社会信用代码")
    private String supplierCreditCode;

    @Schema(description = "报价金额")
    private BigDecimal quotedPrice;

    @Schema(description = "文件检查结果")
    private String documentCheckResult;

    @Schema(description = "开标备注")
    private String openComment;

    @Schema(description = "排序号")
    private Integer sortNo;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
