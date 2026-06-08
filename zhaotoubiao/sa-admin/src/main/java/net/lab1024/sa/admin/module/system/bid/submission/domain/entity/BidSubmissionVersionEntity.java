package net.lab1024.sa.admin.module.system.bid.submission.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 投标版本实体
 *
 * @author Codex
 * @date 2026-06-08
 */
@Data
@TableName("t_bid_submission_version")
public class BidSubmissionVersionEntity {

    @TableId(type = IdType.AUTO)
    private Long submissionVersionId;

    private Long submissionId;

    private Integer versionNo;

    private String receiptNo;

    private LocalDateTime submitTime;

    private Long submitUserId;

    private BigDecimal priceAmount;

    private String contactName;

    private String contactPhone;

    private String fileManifestJson;

    private Boolean effective;

    private Boolean deletedFlag;

    private Long createUserId;

    private LocalDateTime createTime;
}
