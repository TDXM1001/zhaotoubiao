package net.lab1024.sa.admin.module.system.bid.project.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 招标项目视图对象
 *
 * @author Codex
 * @date 2026-06-07
 */
@Data
public class BidProjectVO {

    @Schema(description = "项目ID")
    private Long projectId;

    @Schema(description = "项目编号")
    private String projectCode;

    @Schema(description = "项目名称")
    private String projectName;

    @Schema(description = "项目类型")
    private String projectType;

    @Schema(description = "采购方式")
    private String procurementMode;

    @Schema(description = "招标人归属组织ID")
    private Long ownerOrgId;

    @Schema(description = "招标人归属组织名称")
    private String ownerOrgName;

    @Schema(description = "代理机构归属组织ID")
    private Long agentOrgId;

    @Schema(description = "代理机构归属组织名称")
    private String agentOrgName;

    @Schema(description = "项目负责人员工ID")
    private Long managerEmployeeId;

    @Schema(description = "项目负责人名称")
    private String managerEmployeeName;

    @Schema(description = "项目状态")
    private String status;

    @Schema(description = "项目预算金额")
    private BigDecimal budgetAmount;

    @Schema(description = "标段数量")
    private Long lotCount;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "发布时间")
    private LocalDateTime publishTime;

    @Schema(description = "归档时间")
    private LocalDateTime archiveTime;

    @Schema(description = "版本号")
    private Integer version;

    @Schema(description = "允许动作")
    private List<String> allowedActions;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
