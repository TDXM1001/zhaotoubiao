package net.lab1024.sa.admin.module.system.bid.portal.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 供应商门户登录态信息
 *
 * @author Codex
 * @date 2026-06-08
 */
@Data
public class BidPortalAuthVO {

    @Schema(description = "门户访问 token")
    private String token;

    @Schema(description = "门户账号ID")
    private Long portalAccountId;

    @Schema(description = "供应商企业ID")
    private Long supplierEnterpriseId;

    @Schema(description = "供应商名称")
    private String supplierName;

    @Schema(description = "统一社会信用代码")
    private String supplierCreditCode;

    @Schema(description = "登录账号")
    private String loginName;

    @Schema(description = "联系人")
    private String contactName;

    @Schema(description = "联系人电话")
    private String contactPhone;
}
