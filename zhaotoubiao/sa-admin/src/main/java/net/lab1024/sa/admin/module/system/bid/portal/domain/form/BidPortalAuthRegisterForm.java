package net.lab1024.sa.admin.module.system.bid.portal.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 供应商门户账号注册表单
 *
 * @author Codex
 * @date 2026-06-08
 */
@Data
public class BidPortalAuthRegisterForm {

    @Schema(description = "登录账号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "登录账号不能为空")
    private String loginName;

    @Schema(description = "密码", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "密码不能为空")
    private String password;

    @Schema(description = "统一社会信用代码", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "统一社会信用代码不能为空")
    private String supplierCreditCode;

    @Schema(description = "企业档案联系人手机号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "联系人手机号不能为空")
    private String contactPhone;
}
