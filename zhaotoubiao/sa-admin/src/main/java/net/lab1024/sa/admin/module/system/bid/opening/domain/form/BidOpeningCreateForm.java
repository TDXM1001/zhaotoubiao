package net.lab1024.sa.admin.module.system.bid.opening.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 开标创建表单
 *
 * @author Codex
 * @date 2026-06-08
 */
@Data
public class BidOpeningCreateForm {

    @Schema(description = "项目ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "项目ID不能为空")
    private Long projectId;

    @Schema(description = "标段ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "标段ID不能为空")
    private Long lotId;

    @Schema(description = "开标时间")
    private LocalDateTime openingTime;

    @Schema(description = "开标地点")
    @Size(max = 200, message = "开标地点最多200个字符")
    private String openingPlace;

    @Schema(description = "主持人员工ID")
    private Long hostEmployeeId;

    @Schema(description = "记录人员工ID")
    private Long recorderEmployeeId;

    @Schema(description = "开标摘要")
    @Size(max = 1000, message = "开标摘要最多1000个字符")
    private String summary;
}
