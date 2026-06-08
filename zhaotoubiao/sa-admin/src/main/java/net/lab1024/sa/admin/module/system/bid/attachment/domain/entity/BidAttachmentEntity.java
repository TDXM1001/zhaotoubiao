package net.lab1024.sa.admin.module.system.bid.attachment.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 招投标附件关联实体
 *
 * @author Codex
 * @date 2026-06-08
 */
@Data
@TableName("t_bid_attachment")
public class BidAttachmentEntity {

    @TableId(type = IdType.AUTO)
    private Long attachmentId;

    private String businessType;

    private Long businessId;

    private Long projectId;

    private Long lotId;

    private Long fileId;

    private String fileCategory;

    private Integer versionNo;

    private Integer sortNo;

    @TableField("is_main")
    private Boolean mainFlag;

    private Boolean deletedFlag;

    private Long createUserId;

    private LocalDateTime createTime;
}
