package net.lab1024.sa.admin.module.system.bid.portal.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 门户投标动作表单
 *
 * @author Codex
 * @date 2026-06-08
 */
@Data
public class BidPortalSubmissionActionForm {

    @Schema(description = "投标ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "投标ID不能为空")
    private Long submissionId;

    @Schema(description = "当前供应商统一社会信用代码，由门户登录态写入")
    private String supplierCreditCode;

    @Schema(description = "乐观锁版本号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "版本号不能为空")
    private Integer version;

    @Schema(description = "备注")
    private String remark;
}
