package net.lab1024.sa.admin.module.system.bid.evaluation.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 评标创建表单
 *
 * @author Codex
 * @date 2026-06-08
 */
@Data
public class BidEvaluationCreateForm {

    @Schema(description = "项目ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "项目ID不能为空")
    private Long projectId;

    @Schema(description = "标段ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "标段ID不能为空")
    private Long lotId;

    @Schema(description = "开标ID")
    private Long openingId;

    @Schema(description = "评标方式")
    @Size(max = 64, message = "评标方式最多64个字符")
    private String evaluationMode;

    @Schema(description = "评标摘要")
    @Size(max = 1000, message = "评标摘要最多1000个字符")
    private String finalSummary;
}
