package net.lab1024.sa.admin.module.system.bid.submission.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import net.lab1024.sa.base.common.domain.PageParam;

/**
 * 投标分页查询表单
 *
 * @author Codex
 * @date 2026-06-08
 */
@Data
public class BidSubmissionQueryForm extends PageParam {

    @Schema(description = "关键字，支持项目、标段、供应商、信用代码、回执号")
    private String keyword;

    @Schema(description = "项目ID")
    private Long projectId;

    @Schema(description = "标段ID")
    private Long lotId;

    @Schema(description = "报名ID")
    private Long registrationId;

    @Schema(description = "供应商企业ID")
    private Long supplierEnterpriseId;

    @Schema(description = "投标状态")
    private String status;

    @Schema(description = "删除标识")
    private Boolean deletedFlag;
}
