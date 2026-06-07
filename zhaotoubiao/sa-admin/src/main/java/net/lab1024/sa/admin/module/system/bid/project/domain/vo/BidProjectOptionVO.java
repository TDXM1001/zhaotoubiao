package net.lab1024.sa.admin.module.system.bid.project.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 招标项目下拉选项
 *
 * @author Codex
 * @date 2026-06-07
 */
@Data
public class BidProjectOptionVO {

    @Schema(description = "项目ID")
    private Long projectId;

    @Schema(description = "项目编号")
    private String projectCode;

    @Schema(description = "项目名称")
    private String projectName;

    @Schema(description = "项目状态")
    private String status;
}
