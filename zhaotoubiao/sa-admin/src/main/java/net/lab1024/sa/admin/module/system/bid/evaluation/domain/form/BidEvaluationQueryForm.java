package net.lab1024.sa.admin.module.system.bid.evaluation.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import net.lab1024.sa.base.common.domain.PageParam;

/**
 * 评标分页查询表单
 *
 * @author Codex
 * @date 2026-06-08
 */
@Data
public class BidEvaluationQueryForm extends PageParam {

    @Schema(description = "关键字，支持项目、标段、评标摘要")
    private String keyword;

    @Schema(description = "项目ID")
    private Long projectId;

    @Schema(description = "标段ID")
    private Long lotId;

    @Schema(description = "开标ID")
    private Long openingId;

    @Schema(description = "评标状态")
    private String status;

    @Schema(description = "删除标识")
    private Boolean deletedFlag;
}
