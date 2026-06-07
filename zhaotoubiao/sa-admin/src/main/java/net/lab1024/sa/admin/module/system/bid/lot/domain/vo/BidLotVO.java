package net.lab1024.sa.admin.module.system.bid.lot.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 标段视图对象
 *
 * @author Codex
 * @date 2026-06-07
 */
@Data
public class BidLotVO {

    @Schema(description = "标段ID")
    private Long lotId;

    @Schema(description = "所属项目ID")
    private Long projectId;

    @Schema(description = "所属项目名称")
    private String projectName;

    @Schema(description = "标段编号")
    private String lotCode;

    @Schema(description = "标段名称")
    private String lotName;

    @Schema(description = "标段序号")
    private Integer lotNo;

    @Schema(description = "标段范围说明")
    private String lotScope;

    @Schema(description = "状态")
    private String status;

    @Schema(description = "预算金额")
    private BigDecimal budgetAmount;

    @Schema(description = "投标开始时间")
    private LocalDateTime bidStartTime;

    @Schema(description = "投标截止时间")
    private LocalDateTime bidEndTime;

    @Schema(description = "开标时间")
    private LocalDateTime openingTime;

    @Schema(description = "评标方式")
    private String evaluationMode;

    @Schema(description = "定标方式")
    private String awardMode;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "版本号")
    private Integer version;

    @Schema(description = "允许动作")
    private List<String> allowedActions;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
