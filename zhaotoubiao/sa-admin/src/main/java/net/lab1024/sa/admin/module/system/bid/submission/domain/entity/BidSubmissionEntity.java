package net.lab1024.sa.admin.module.system.bid.submission.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 投标主记录实体
 *
 * @author Codex
 * @date 2026-06-08
 */
@Data
@TableName("t_bid_submission")
public class BidSubmissionEntity {

    @TableId(type = IdType.AUTO)
    private Long submissionId;

    private Long projectId;

    private Long lotId;

    private Long registrationId;

    private Long supplierEnterpriseId;

    private String supplierNameSnapshot;

    private String supplierCreditCode;

    private String status;

    private Integer latestVersionNo;

    private LocalDateTime latestSubmitTime;

    private String receiptNo;

    private LocalDateTime withdrawTime;

    private String withdrawReason;

    private String remark;

    private Boolean deletedFlag;

    private Integer version;

    private Long createUserId;

    private LocalDateTime createTime;

    private Long updateUserId;

    private LocalDateTime updateTime;
}
