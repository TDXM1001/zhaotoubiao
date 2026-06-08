package net.lab1024.sa.admin.module.system.bid.attachment.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 招投标附件关联创建表单
 *
 * @author Codex
 * @date 2026-06-08
 */
@Data
public class BidAttachmentCreateForm {

    @Schema(description = "文件ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "文件ID不能为空")
    private Long fileId;

    @Schema(description = "文件分类", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "文件分类不能为空")
    @Size(max = 64, message = "文件分类最多64个字符")
    private String fileCategory;

    @Schema(description = "业务版本号")
    private Integer versionNo;

    @Schema(description = "排序号")
    private Integer sortNo;

    @Schema(description = "是否主文件")
    private Boolean mainFlag;
}
