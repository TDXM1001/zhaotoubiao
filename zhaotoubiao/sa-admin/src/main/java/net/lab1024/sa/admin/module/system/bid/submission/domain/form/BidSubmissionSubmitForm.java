package net.lab1024.sa.admin.module.system.bid.submission.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 投标提交表单
 *
 * @author Codex
 * @date 2026-06-08
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class BidSubmissionSubmitForm extends BidSubmissionActionForm {

    @Schema(description = "投标报价金额")
    private BigDecimal priceAmount;

    @Schema(description = "联系人")
    private String contactName;

    @Schema(description = "联系人电话")
    private String contactPhone;

    @Schema(description = "投标文件清单JSON快照")
    private String fileManifestJson;
}
