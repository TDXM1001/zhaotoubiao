package net.lab1024.sa.admin.module.system.bid.opening.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 开标主记录实体
 *
 * @author Codex
 * @date 2026-06-08
 */
@Data
@TableName("t_bid_opening")
public class BidOpeningEntity {

    @TableId(type = IdType.AUTO)
    private Long openingId;

    private Long projectId;

    private Long lotId;

    private String status;

    private LocalDateTime openingTime;

    private String openingPlace;

    private Long hostEmployeeId;

    private Long recorderEmployeeId;

    private String summary;

    private Boolean abnormalFlag;

    private String abnormalReason;

    private Boolean deletedFlag;

    private Integer version;

    private Long createUserId;

    private LocalDateTime createTime;

    private Long updateUserId;

    private LocalDateTime updateTime;
}
