package net.lab1024.sa.admin.module.system.bid.tender.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import net.lab1024.sa.admin.module.system.bid.attachment.domain.vo.BidAttachmentVO;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 招标文件版本视图对象
 *
 * @author Codex
 * @date 2026-06-08
 */
@Data
public class BidTenderVO {

    @Schema(description = "招标文件版本ID")
    private Long tenderVersionId;

    @Schema(description = "项目ID")
    private Long projectId;

    @Schema(description = "项目名称")
    private String projectName;

    @Schema(description = "标段ID")
    private Long lotId;

    @Schema(description = "标段编号")
    private String lotCode;

    @Schema(description = "标段名称")
    private String lotName;

    @Schema(description = "版本号")
    private Integer versionNo;

    @Schema(description = "版本类型")
    private String versionType;

    @Schema(description = "状态")
    private String status;

    @Schema(description = "是否当前有效版本")
    private Boolean currentFlag;

    @Schema(description = "父版本ID")
    private Long parentVersionId;

    @Schema(description = "生效时间")
    private LocalDateTime effectiveTime;

    @Schema(description = "发布时间")
    private LocalDateTime publishTime;

    @Schema(description = "版本摘要")
    private String summary;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "乐观锁版本号")
    private Integer version;

    @Schema(description = "允许动作")
    private List<String> allowedActions;

    @Schema(description = "附件摘要列表")
    private List<BidAttachmentVO> attachments;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
