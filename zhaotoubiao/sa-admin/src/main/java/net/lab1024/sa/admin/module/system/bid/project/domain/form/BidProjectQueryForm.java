package net.lab1024.sa.admin.module.system.bid.project.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import net.lab1024.sa.base.common.domain.PageParam;

/**
 * 招标项目分页查询表单
 *
 * @author Codex
 * @date 2026-06-07
 */
@Data
public class BidProjectQueryForm extends PageParam {

    @Schema(description = "关键字")
    private String keyword;

    @Schema(description = "项目类型")
    private String projectType;

    @Schema(description = "采购方式")
    private String procurementMode;

    @Schema(description = "项目状态")
    private String status;

    @Schema(description = "招标人归属组织ID")
    private Long ownerOrgId;

    @Schema(hidden = true)
    private Boolean deletedFlag;
}
