package net.lab1024.sa.admin.module.system.bid.submission.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import net.lab1024.sa.admin.module.system.bid.attachment.domain.vo.BidAttachmentVO;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 投标视图对象
 *
 * @author Codex
 * @date 2026-06-08
 */
@Data
public class BidSubmissionVO {

    @Schema(description = "投标ID")
    private Long submissionId;

    @Schema(description = "最新投标版本ID")
    private Long latestSubmissionVersionId;

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

    @Schema(description = "报名ID")
    private Long registrationId;

    @Schema(description = "供应商企业ID")
    private Long supplierEnterpriseId;

    @Schema(description = "供应商名称快照")
    private String supplierNameSnapshot;

    @Schema(description = "统一社会信用代码")
    private String supplierCreditCode;

    @Schema(description = "投标状态")
    private String status;

    @Schema(description = "最新投标版本号")
    private Integer latestVersionNo;

    @Schema(description = "最近提交时间")
    private LocalDateTime latestSubmitTime;

    @Schema(description = "投标回执号")
    private String receiptNo;

    @Schema(description = "最新报价金额")
    private BigDecimal priceAmount;

    @Schema(description = "最新联系人")
    private String contactName;

    @Schema(description = "最新联系人电话")
    private String contactPhone;

    @Schema(description = "最新投标文件清单JSON快照")
    private String fileManifestJson;

    @Schema(description = "撤回时间")
    private LocalDateTime withdrawTime;

    @Schema(description = "撤回原因")
    private String withdrawReason;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "版本号")
    private Integer version;

    @Schema(description = "允许动作")
    private List<String> allowedActions;

    @Schema(description = "附件摘要列表")
    private List<BidAttachmentVO> attachments;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
