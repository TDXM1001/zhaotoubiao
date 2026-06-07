package net.lab1024.sa.admin.module.system.bid.registration.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 供应商报名动作表单
 *
 * @author Codex
 * @date 2026-06-07
 */
@Data
public class BidRegistrationActionForm {

    @Schema(description = "报名ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "报名ID不能为空")
    private Long registrationId;

    @Schema(description = "版本号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "版本号不能为空")
    private Integer version;

    @Schema(description = "审核备注或驳回原因")
    private String remark;
}
