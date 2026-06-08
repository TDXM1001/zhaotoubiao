package net.lab1024.sa.admin.module.system.bid.portal.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import net.lab1024.sa.admin.module.system.bid.attachment.domain.vo.BidAttachmentVO;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 门户标段视图对象
 *
 * @author Codex
 * @date 2026-06-08
 */
@Data
public class BidPortalLotVO {

    @Schema(description = "标段ID")
    private Long lotId;

    @Schema(description = "标段编号")
    private String lotCode;

    @Schema(description = "标段名称")
    private String lotName;

    @Schema(description = "标段范围")
    private String lotScope;

    @Schema(description = "报名开始时间")
    private LocalDateTime registrationStartTime;

    @Schema(description = "报名截止时间")
    private LocalDateTime registrationEndTime;

    @Schema(description = "投标开始时间")
    private LocalDateTime bidStartTime;

    @Schema(description = "投标截止时间")
    private LocalDateTime bidEndTime;

    @Schema(description = "当前有效招标文件版本ID")
    private Long tenderVersionId;

    @Schema(description = "当前有效招标文件版本号")
    private Integer tenderVersionNo;

    @Schema(description = "招标文件摘要")
    private String tenderSummary;

    @Schema(description = "招标文件附件摘要")
    private List<BidAttachmentVO> tenderAttachments;
}
