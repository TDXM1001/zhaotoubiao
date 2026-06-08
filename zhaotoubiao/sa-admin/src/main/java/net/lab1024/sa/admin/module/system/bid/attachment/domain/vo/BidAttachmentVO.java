package net.lab1024.sa.admin.module.system.bid.attachment.domain.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 招投标附件摘要
 *
 * @author Codex
 * @date 2026-06-08
 */
@Data
public class BidAttachmentVO {

    @Schema(description = "附件关联ID")
    private Long attachmentId;

    @Schema(description = "业务类型")
    private String businessType;

    @Schema(description = "业务对象ID")
    private Long businessId;

    @Schema(description = "文件ID")
    private Long fileId;

    @Schema(description = "文件名称")
    private String fileName;

    @Schema(description = "文件大小")
    private Long fileSize;

    @Schema(description = "文件分类")
    private String fileCategory;

    @Schema(description = "业务版本号")
    private Integer versionNo;

    @Schema(description = "是否主文件")
    private Boolean mainFlag;

    @Schema(description = "下载地址")
    private String downloadUrl;

    @Schema(description = "预览地址")
    private String previewUrl;

    @Schema(description = "上传时间")
    private LocalDateTime uploadTime;

    @Schema(description = "上传人")
    private String uploaderName;

    @JsonIgnore
    private String fileKey;

    /**
     * 基于业务资源路径生成访问地址，避免前端绕过业务权限直接访问文件模块。
     */
    public void fillBusinessUrls(String businessResourcePath) {
        if (businessResourcePath == null || businessResourcePath.isBlank() || attachmentId == null) {
            return;
        }
        String normalizedPath = businessResourcePath.endsWith("/")
                ? businessResourcePath.substring(0, businessResourcePath.length() - 1)
                : businessResourcePath;
        this.downloadUrl = normalizedPath + "/attachments/" + attachmentId + "/download";
        this.previewUrl = normalizedPath + "/attachments/" + attachmentId + "/preview-url";
    }
}
