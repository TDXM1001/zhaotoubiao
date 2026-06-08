package net.lab1024.sa.admin.module.system.bid.evaluation.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 评标视图对象
 *
 * @author Codex
 * @date 2026-06-08
 */
@Data
public class BidEvaluationVO {

    @Schema(description = "评标ID")
    private Long evaluationId;

    @Schema(description = "项目ID")
    private Long projectId;

    @Schema(description = "项目名称")
    private String projectName;

    @Schema(description = "标段ID")
    private Long lotId;

    @Schema(description = "标段编号")
    private String lotCode;

    @Schema(description = "标段名称")
    private String lotName;

    @Schema(description = "开标ID")
    private Long openingId;

    @Schema(description = "评标状态")
    private String status;

    @Schema(description = "评标方式")
    private String evaluationMode;

    @Schema(description = "开始时间")
    private LocalDateTime startTime;

    @Schema(description = "定稿时间")
    private LocalDateTime finalizeTime;

    @Schema(description = "评标摘要")
    private String finalSummary;

    @Schema(description = "回退原因")
    private String rollbackReason;

    @Schema(description = "版本号")
    private Integer version;

    @Schema(description = "允许动作")
    private List<String> allowedActions;

    @Schema(description = "评标明细")
    private List<BidEvaluationItemVO> itemList;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
