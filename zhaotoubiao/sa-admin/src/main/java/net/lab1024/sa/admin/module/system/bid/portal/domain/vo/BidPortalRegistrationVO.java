package net.lab1024.sa.admin.module.system.bid.portal.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 门户报名视图对象
 *
 * @author Codex
 * @date 2026-06-08
 */
@Data
public class BidPortalRegistrationVO {

    @Schema(description = "报名ID")
    private Long registrationId;

    @Schema(description = "项目ID")
    private Long projectId;

    @Schema(description = "项目名称")
    private String projectName;

    @Schema(description = "标段ID")
    private Long lotId;

    @Schema(description = "标段名称")
    private String lotName;

    @Schema(description = "供应商名称")
    private String supplierNameSnapshot;

    @Schema(description = "统一社会信用代码")
    private String supplierCreditCode;

    @Schema(description = "报名状态")
    private String status;

    @Schema(description = "提交时间")
    private LocalDateTime submitTime;

    @Schema(description = "资格审核时间")
    private LocalDateTime qualifiedTime;

    @Schema(description = "驳回原因")
    private String rejectReason;

    @Schema(description = "取消原因")
    private String cancelReason;

    @Schema(description = "版本号")
    private Integer version;
}
