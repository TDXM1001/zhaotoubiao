package net.lab1024.sa.admin.module.system.bid.portal.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 门户项目视图对象
 *
 * @author Codex
 * @date 2026-06-08
 */
@Data
public class BidPortalProjectVO {

    @Schema(description = "项目ID")
    private Long projectId;

    @Schema(description = "项目编号")
    private String projectCode;

    @Schema(description = "项目名称")
    private String projectName;

    @Schema(description = "项目类型")
    private String projectType;

    @Schema(description = "采购方式")
    private String procurementMode;

    @Schema(description = "预算金额")
    private BigDecimal budgetAmount;

    @Schema(description = "发布时间")
    private LocalDateTime publishTime;

    @Schema(description = "可报名标段数")
    private Integer lotCount;

    @Schema(description = "可见标段列表")
    private List<BidPortalLotVO> lots;
}
