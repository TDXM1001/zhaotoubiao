package net.lab1024.sa.admin.module.system.bid.opening.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 开标明细实体
 *
 * @author Codex
 * @date 2026-06-08
 */
@Data
@TableName("t_bid_opening_item")
public class BidOpeningItemEntity {

    @TableId(type = IdType.AUTO)
    private Long openingItemId;

    private Long openingId;

    private Long submissionId;

    private Long submissionVersionId;

    private Long supplierEnterpriseId;

    private String supplierNameSnapshot;

    private String supplierCreditCode;

    private BigDecimal quotedPrice;

    private String documentCheckResult;

    private String openComment;

    private Integer sortNo;

    private Boolean deletedFlag;

    private LocalDateTime createTime;
}
