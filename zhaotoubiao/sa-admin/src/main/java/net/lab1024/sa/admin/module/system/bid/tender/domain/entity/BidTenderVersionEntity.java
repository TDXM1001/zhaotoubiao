package net.lab1024.sa.admin.module.system.bid.tender.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 招标文件版本实体
 *
 * @author Codex
 * @date 2026-06-08
 */
@Data
@TableName("t_bid_tender_version")
public class BidTenderVersionEntity {

    @TableId(type = IdType.AUTO)
    private Long tenderVersionId;

    private Long projectId;

    private Long lotId;

    private Integer versionNo;

    private String versionType;

    private String status;

    private Boolean currentFlag;

    private Long parentVersionId;

    private LocalDateTime effectiveTime;

    private Long publishUserId;

    private LocalDateTime publishTime;

    private String summary;

    private String remark;

    private Boolean deletedFlag;

    private Integer version;

    private Long createUserId;

    private LocalDateTime createTime;

    private Long updateUserId;

    private LocalDateTime updateTime;
}
