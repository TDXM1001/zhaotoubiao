package net.lab1024.sa.admin.module.system.bid.project.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 招标项目新增表单
 *
 * @author Codex
 * @date 2026-06-07
 */
@Data
public class BidProjectAddForm {

    @Schema(description = "项目编号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "项目编号不能为空")
    private String projectCode;

    @Schema(description = "项目名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "项目名称不能为空")
    private String projectName;

    @Schema(description = "项目类型", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "项目类型不能为空")
    private String projectType;

    @Schema(description = "采购方式", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "采购方式不能为空")
    private String procurementMode;

    @Schema(description = "招标人归属组织ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "招标人归属组织不能为空")
    private Long ownerOrgId;

    @Schema(description = "代理机构归属组织ID")
    private Long agentOrgId;

    @Schema(description = "项目负责人员工ID")
    private Long managerEmployeeId;

    @Schema(description = "项目预算金额")
    private BigDecimal budgetAmount;

    @Schema(description = "备注")
    private String remark;
}
