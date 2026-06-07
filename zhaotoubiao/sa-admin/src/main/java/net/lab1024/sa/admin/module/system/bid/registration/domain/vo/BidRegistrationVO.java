package net.lab1024.sa.admin.module.system.bid.registration.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 供应商报名视图对象
 *
 * @author Codex
 * @date 2026-06-07
 */
@Data
public class BidRegistrationVO {

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

    @Schema(description = "标段编号")
    private String lotCode;

    @Schema(description = "供应商企业ID")
    private Long supplierEnterpriseId;

    @Schema(description = "供应商名称快照")
    private String supplierNameSnapshot;

    @Schema(description = "统一社会信用代码")
    private String supplierCreditCode;

    @Schema(description = "联系人")
    private String contactName;

    @Schema(description = "联系人电话")
    private String contactPhone;

    @Schema(description = "联系人邮箱")
    private String contactEmail;

    @Schema(description = "报名方式")
    private String registrationType;

    @Schema(description = "报名状态")
    private String status;

    @Schema(description = "提交时间")
    private LocalDateTime submitTime;

    @Schema(description = "资格结果")
    private String qualifiedResult;

    @Schema(description = "资格审核时间")
    private LocalDateTime qualifiedTime;

    @Schema(description = "资格审核人")
    private Long qualifiedBy;

    @Schema(description = "驳回原因")
    private String rejectReason;

    @Schema(description = "取消时间")
    private LocalDateTime cancelTime;

    @Schema(description = "取消原因")
    private String cancelReason;

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
