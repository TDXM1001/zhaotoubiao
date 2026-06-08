package net.lab1024.sa.admin.module.system.bid.opening.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 开标视图对象
 *
 * @author Codex
 * @date 2026-06-08
 */
@Data
public class BidOpeningVO {

    @Schema(description = "开标ID")
    private Long openingId;

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

    @Schema(description = "开标状态")
    private String status;

    @Schema(description = "开标时间")
    private LocalDateTime openingTime;

    @Schema(description = "开标地点")
    private String openingPlace;

    @Schema(description = "主持人员工ID")
    private Long hostEmployeeId;

    @Schema(description = "记录人员工ID")
    private Long recorderEmployeeId;

    @Schema(description = "开标摘要")
    private String summary;

    @Schema(description = "是否异常")
    private Boolean abnormalFlag;

    @Schema(description = "异常原因")
    private String abnormalReason;

    @Schema(description = "版本号")
    private Integer version;

    @Schema(description = "允许动作")
    private List<String> allowedActions;

    @Schema(description = "开标明细")
    private List<BidOpeningItemVO> itemList;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
