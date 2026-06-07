package net.lab1024.sa.admin.module.system.bid.registration.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import net.lab1024.sa.base.common.domain.PageParam;

/**
 * 供应商报名分页查询表单
 *
 * @author Codex
 * @date 2026-06-07
 */
@Data
public class BidRegistrationQueryForm extends PageParam {

    @Schema(description = "关键字，支持项目、标段、供应商、信用代码")
    private String keyword;

    @Schema(description = "项目ID")
    private Long projectId;

    @Schema(description = "标段ID")
    private Long lotId;

    @Schema(description = "报名状态")
    private String status;

    @Schema(description = "报名方式")
    private String registrationType;

    @Schema(description = "删除标识")
    private Boolean deletedFlag;
}
