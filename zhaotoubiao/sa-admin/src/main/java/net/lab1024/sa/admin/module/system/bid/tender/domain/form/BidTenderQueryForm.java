package net.lab1024.sa.admin.module.system.bid.tender.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import net.lab1024.sa.base.common.domain.PageParam;

/**
 * 招标文件分页查询表单
 *
 * @author Codex
 * @date 2026-06-08
 */
@Data
public class BidTenderQueryForm extends PageParam {

    @Schema(description = "关键字")
    private String keyword;

    @Schema(description = "项目ID")
    private Long projectId;

    @Schema(description = "标段ID")
    private Long lotId;

    @Schema(description = "版本类型")
    private String versionType;

    @Schema(description = "状态")
    private String status;

    @Schema(hidden = true)
    private Boolean deletedFlag;
}
