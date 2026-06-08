package net.lab1024.sa.admin.module.system.bid.tender.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 招标文件新增表单
 *
 * @author Codex
 * @date 2026-06-08
 */
@Data
public class BidTenderCreateForm {

    @Schema(description = "项目ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "项目ID不能为空")
    private Long projectId;

    @Schema(description = "标段ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "标段ID不能为空")
    private Long lotId;

    @Schema(description = "版本类型", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "版本类型不能为空")
    private String versionType;

    @Schema(description = "版本摘要")
    private String summary;

    @Schema(description = "备注")
    private String remark;
}
