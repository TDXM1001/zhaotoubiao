package net.lab1024.sa.admin.module.system.bid.portal.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.base.common.domain.PageParam;

/**
 * 门户项目查询表单
 *
 * @author Codex
 * @date 2026-06-08
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class BidPortalProjectQueryForm extends PageParam {

    @Schema(description = "关键字")
    private String keyword;
}
