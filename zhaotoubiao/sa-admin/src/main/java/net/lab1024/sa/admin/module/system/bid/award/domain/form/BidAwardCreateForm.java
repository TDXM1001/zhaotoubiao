package net.lab1024.sa.admin.module.system.bid.award.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 定标创建表单
 *
 * @author Codex
 * @date 2026-06-08
 */
@Data
public class BidAwardCreateForm {

    @Schema(description = "项目ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "项目ID不能为空")
    private Long projectId;

    @Schema(description = "标段ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "标段ID不能为空")
    private Long lotId;

    @Schema(description = "评标ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "评标ID不能为空")
    private Long evaluationId;

    @Schema(description = "中标投标ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "中标投标ID不能为空")
    private Long winningSubmissionId;

    @Schema(description = "中标金额")
    private BigDecimal awardAmount;

    @Schema(description = "备注")
    @Size(max = 500, message = "备注最多500个字符")
    private String remark;
}
