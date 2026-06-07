package net.lab1024.sa.admin.module.system.bid.lot.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import net.lab1024.sa.base.common.domain.PageParam;

/**
 * 标段分页查询表单
 *
 * @author Codex
 * @date 2026-06-07
 */
@Data
public class BidLotQueryForm extends PageParam {

    @Schema(description = "关键字")
    private String keyword;

    @Schema(description = "所属项目ID")
    private Long projectId;

    @Schema(description = "状态")
    private String status;

    @Schema(hidden = true)
    private Boolean deletedFlag;
}
