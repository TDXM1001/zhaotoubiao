package net.lab1024.sa.admin.module.system.bid.portal.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 门户报名创建表单
 *
 * @author Codex
 * @date 2026-06-08
 */
@Data
public class BidPortalRegistrationCreateForm {

    @Schema(description = "项目ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "项目ID不能为空")
    private Long projectId;

    @Schema(description = "标段ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "标段ID不能为空")
    private Long lotId;

    @Schema(description = "供应商企业ID")
    private Long supplierEnterpriseId;

    @Schema(description = "供应商名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "供应商名称不能为空")
    private String supplierNameSnapshot;

    @Schema(description = "统一社会信用代码", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "统一社会信用代码不能为空")
    private String supplierCreditCode;

    @Schema(description = "联系人")
    private String contactName;

    @Schema(description = "联系人电话")
    private String contactPhone;

    @Schema(description = "联系人邮箱")
    private String contactEmail;

    @Schema(description = "报名方式", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "报名方式不能为空")
    private String registrationType;

    @Schema(description = "备注")
    private String remark;
}
