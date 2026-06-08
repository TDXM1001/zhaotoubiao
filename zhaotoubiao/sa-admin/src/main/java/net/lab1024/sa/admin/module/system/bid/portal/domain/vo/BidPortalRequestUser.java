package net.lab1024.sa.admin.module.system.bid.portal.domain.vo;

import lombok.Data;
import net.lab1024.sa.base.common.domain.RequestUser;
import net.lab1024.sa.base.common.enumeration.UserTypeEnum;

/**
 * 供应商门户请求用户
 *
 * @author Codex
 * @date 2026-06-08
 */
@Data
public class BidPortalRequestUser implements RequestUser {

    private Long portalAccountId;

    private Long supplierEnterpriseId;

    private String supplierName;

    private String supplierCreditCode;

    private String loginName;

    private String contactName;

    private String contactPhone;

    private UserTypeEnum userType = UserTypeEnum.BID_PORTAL_SUPPLIER;

    private String ip;

    private String userAgent;

    @Override
    public Long getUserId() {
        return portalAccountId;
    }

    @Override
    public String getUserName() {
        return supplierName;
    }
}
