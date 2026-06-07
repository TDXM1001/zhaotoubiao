package net.lab1024.sa.admin.module.system.bid.lot.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 标段新增表单
 *
 * @author Codex
 * @date 2026-06-07
 */
@Data
public class BidLotAddForm {

    @Schema(description = "所属项目ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "所属项目不能为空")
    private Long projectId;

    @Schema(description = "标段编号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "标段编号不能为空")
    private String lotCode;

    @Schema(description = "标段名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "标段名称不能为空")
    private String lotName;

    @Schema(description = "标段序号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "标段序号不能为空")
    private Integer lotNo;

    @Schema(description = "标段范围说明")
    private String lotScope;

    @Schema(description = "标段预算金额")
    private BigDecimal budgetAmount;

    @Schema(description = "报名开始时间")
    private LocalDateTime registrationStartTime;

    @Schema(description = "报名截止时间")
    private LocalDateTime registrationEndTime;

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
}
