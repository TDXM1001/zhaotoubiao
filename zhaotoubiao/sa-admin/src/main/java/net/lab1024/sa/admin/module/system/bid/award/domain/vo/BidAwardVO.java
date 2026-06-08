package net.lab1024.sa.admin.module.system.bid.award.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 定标视图对象
 *
 * @author Codex
 * @date 2026-06-08
 */
@Data
public class BidAwardVO {

    @Schema(description = "定标ID")
    private Long awardId;

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

    @Schema(description = "评标ID")
    private Long evaluationId;

    @Schema(description = "定标状态")
    private String status;

    @Schema(description = "中标投标ID")
    private Long winningSubmissionId;

    @Schema(description = "中标供应商企业ID")
    private Long winnerEnterpriseId;

    @Schema(description = "中标供应商名称")
    private String winnerNameSnapshot;

    @Schema(description = "中标供应商信用代码")
    private String winnerCreditCode;

    @Schema(description = "中标金额")
    private BigDecimal awardAmount;

    @Schema(description = "确认人")
    private Long confirmUserId;

    @Schema(description = "确认时间")
    private LocalDateTime confirmTime;

    @Schema(description = "结果公示时间")
    private LocalDateTime publicNoticeTime;

    @Schema(description = "回退原因")
    private String rollbackReason;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "版本号")
    private Integer version;

    @Schema(description = "允许动作")
    private List<String> allowedActions;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
