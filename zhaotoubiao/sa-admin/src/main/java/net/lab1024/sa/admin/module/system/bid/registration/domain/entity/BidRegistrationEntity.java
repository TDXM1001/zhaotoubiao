package net.lab1024.sa.admin.module.system.bid.registration.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 供应商报名实体
 *
 * @author Codex
 * @date 2026-06-07
 */
@Data
@TableName("t_bid_registration")
public class BidRegistrationEntity {

    @TableId(type = IdType.AUTO)
    private Long registrationId;

    private Long projectId;

    private Long lotId;

    private Long supplierEnterpriseId;

    private String supplierNameSnapshot;

    private String supplierCreditCode;

    private String contactName;

    private String contactPhone;

    private String contactEmail;

    private String registrationType;

    private String status;

    private LocalDateTime submitTime;

    private String qualifiedResult;

    private LocalDateTime qualifiedTime;

    private Long qualifiedBy;

    private String rejectReason;

    private LocalDateTime cancelTime;

    private String cancelReason;

    private String remark;

    private Boolean deletedFlag;

    private Integer version;

    private Long createUserId;

    private LocalDateTime createTime;

    private Long updateUserId;

    private LocalDateTime updateTime;
}
