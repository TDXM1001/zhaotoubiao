package net.lab1024.sa.admin.module.system.bid.portal.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 门户投标主记录创建表单
 *
 * @author Codex
 * @date 2026-06-08
 */
@Data
public class BidPortalSubmissionCreateForm {

    @Schema(description = "项目ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "项目ID不能为空")
    private Long projectId;

    @Schema(description = "标段ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "标段ID不能为空")
    private Long lotId;

    @Schema(description = "报名ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "报名ID不能为空")
    private Long registrationId;

    @Schema(description = "当前供应商统一社会信用代码", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "统一社会信用代码不能为空")
    private String supplierCreditCode;

    @Schema(description = "备注")
    private String remark;
}
