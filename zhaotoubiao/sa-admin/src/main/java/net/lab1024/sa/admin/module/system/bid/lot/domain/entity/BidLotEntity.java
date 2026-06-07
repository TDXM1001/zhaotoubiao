package net.lab1024.sa.admin.module.system.bid.lot.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 标段实体
 *
 * @author Codex
 * @date 2026-06-07
 */
@Data
@TableName("t_bid_lot")
public class BidLotEntity {

    @TableId(type = IdType.AUTO)
    private Long lotId;

    private Long projectId;

    private String lotCode;

    private String lotName;

    private Integer lotNo;

    private String lotScope;

    private String status;

    private BigDecimal budgetAmount;

    private LocalDateTime bidStartTime;

    private LocalDateTime bidEndTime;

    private LocalDateTime openingTime;

    private String evaluationMode;

    private String awardMode;

    private String remark;

    private Boolean deletedFlag;

    private Integer version;

    private Long createUserId;

    private LocalDateTime createTime;

    private Long updateUserId;

    private LocalDateTime updateTime;
}
