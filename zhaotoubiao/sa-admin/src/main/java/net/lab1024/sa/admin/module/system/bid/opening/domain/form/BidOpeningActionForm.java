package net.lab1024.sa.admin.module.system.bid.opening.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 开标动作表单
 *
 * @author Codex
 * @date 2026-06-08
 */
@Data
public class BidOpeningActionForm {

    @Schema(description = "开标ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "开标ID不能为空")
    private Long openingId;

    @Schema(description = "版本号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "版本号不能为空")
    private Integer version;

    @Schema(description = "备注")
    private String remark;
}
