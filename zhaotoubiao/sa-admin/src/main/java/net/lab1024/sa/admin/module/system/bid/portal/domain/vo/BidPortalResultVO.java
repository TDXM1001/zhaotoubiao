package net.lab1024.sa.admin.module.system.bid.portal.domain.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 供应商门户标段结果视图对象
 *
 * @author Codex
 * @date 2026-06-08
 */
@Data
public class BidPortalResultVO {

    @Schema(description = "标段ID")
    private Long lotId;

    @Schema(description = "项目ID")
    private Long projectId;

    @Schema(description = "项目名称")
    private String projectName;

    @Schema(description = "标段编号")
    private String lotCode;

    @Schema(description = "标段名称")
    private String lotName;

    @Schema(description = "定标状态")
    private String awardStatus;

    @Schema(description = "结果是否已公示")
    private Boolean resultVisible;

    @Schema(description = "当前供应商是否中标")
    private Boolean winnerFlag;

    @Schema(description = "中标金额")
    private BigDecimal awardAmount;

    @Schema(description = "结果公示时间")
    private LocalDateTime publicNoticeTime;

    @Schema(description = "提示信息")
    private String message;

    @JsonIgnore
    @Schema(description = "当前供应商是否参与该标段")
    private Boolean participatedFlag;
}
