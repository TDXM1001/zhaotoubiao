package net.lab1024.sa.admin.module.system.bid.submission.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 投标动作表单
 *
 * @author Codex
 * @date 2026-06-08
 */
@Data
public class BidSubmissionActionForm {

    @Schema(description = "投标ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "投标ID不能为空")
    private Long submissionId;

    @Schema(description = "版本号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "版本号不能为空")
    private Integer version;

    @Schema(description = "备注")
    private String remark;
}
