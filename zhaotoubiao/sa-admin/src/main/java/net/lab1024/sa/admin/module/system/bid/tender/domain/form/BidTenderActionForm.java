package net.lab1024.sa.admin.module.system.bid.tender.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 招标文件动作表单
 *
 * @author Codex
 * @date 2026-06-08
 */
@Data
public class BidTenderActionForm {

    @Schema(description = "招标文件版本ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "招标文件版本ID不能为空")
    private Long tenderVersionId;

    @Schema(description = "版本号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "版本号不能为空")
    private Integer version;

    @Schema(description = "备注")
    private String remark;
}
