package net.lab1024.sa.admin.module.system.bid.portal.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 供应商门户账号实体
 *
 * @author Codex
 * @date 2026-06-08
 */
@Data
@TableName("t_bid_portal_account")
public class BidPortalAccountEntity {

    @TableId(type = IdType.AUTO)
    private Long portalAccountId;

    private Long supplierEnterpriseId;

    private String supplierName;

    private String supplierCreditCode;

    private String loginName;

    private String loginPwd;

    private String contactName;

    private String contactPhone;

    private Boolean disabledFlag;

    private Boolean deletedFlag;

    private LocalDateTime lastLoginTime;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
