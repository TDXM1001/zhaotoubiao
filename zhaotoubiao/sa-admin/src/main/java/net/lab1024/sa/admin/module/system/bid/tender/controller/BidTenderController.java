package net.lab1024.sa.admin.module.system.bid.tender.controller;

import cn.hutool.extra.servlet.JakartaServletUtil;
import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import net.lab1024.sa.admin.constant.AdminSwaggerTagConst;
import net.lab1024.sa.admin.module.system.bid.attachment.domain.form.BidAttachmentCreateForm;
import net.lab1024.sa.admin.module.system.bid.tender.domain.form.BidTenderActionForm;
import net.lab1024.sa.admin.module.system.bid.tender.domain.form.BidTenderCreateForm;
import net.lab1024.sa.admin.module.system.bid.tender.domain.form.BidTenderQueryForm;
import net.lab1024.sa.admin.module.system.bid.tender.domain.form.BidTenderUpdateForm;
import net.lab1024.sa.admin.module.system.bid.tender.domain.vo.BidTenderVO;
import net.lab1024.sa.admin.module.system.bid.tender.service.BidTenderService;
import net.lab1024.sa.base.common.constant.RequestHeaderConst;
import net.lab1024.sa.base.common.domain.PageResult;
import net.lab1024.sa.base.common.domain.ResponseDTO;
import net.lab1024.sa.base.common.util.SmartResponseUtil;
import net.lab1024.sa.base.module.support.file.domain.vo.FileDownloadVO;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Objects;

/**
 * 招标文件 Controller
 *
 * @author Codex
 * @date 2026-06-08
 */
@RestController
@Tag(name = AdminSwaggerTagConst.System.SYSTEM_BID_TENDER)
public class BidTenderController {

    @Resource
    private BidTenderService bidTenderService;

    @Operation(summary = "资源化分页查询招标文件")
    @PostMapping("/bid/tenders/search")
    @SaCheckPermission("bid:tender:query")
    public ResponseDTO<PageResult<BidTenderVO>> search(@RequestBody @Valid BidTenderQueryForm queryForm) {
        return ResponseDTO.ok(bidTenderService.queryPage(queryForm));
    }

    @Operation(summary = "资源化查询招标文件详情")
    @GetMapping("/bid/tenders/{tenderVersionId}")
    @SaCheckPermission("bid:tender:query")
    public ResponseDTO<BidTenderVO> getDetail(@PathVariable Long tenderVersionId) {
        return bidTenderService.getDetail(tenderVersionId);
    }

    @Operation(summary = "资源化查询标段当前有效招标文件")
    @GetMapping("/bid/lots/{lotId}/tenders/active")
    @SaCheckPermission("bid:tender:query")
    public ResponseDTO<BidTenderVO> getActiveByLotId(@PathVariable Long lotId) {
        return bidTenderService.getActiveByLotId(lotId);
    }

    @Operation(summary = "资源化新增招标文件")
    @PostMapping("/bid/tenders")
    @SaCheckPermission("bid:tender:create")
    public ResponseDTO<String> create(@RequestBody @Valid BidTenderCreateForm createForm) {
        return bidTenderService.create(createForm);
    }

    @Operation(summary = "资源化编辑招标文件")
    @PutMapping("/bid/tenders/{tenderVersionId}")
    @SaCheckPermission("bid:tender:update")
    public ResponseDTO<String> update(@PathVariable Long tenderVersionId, @RequestBody @Valid BidTenderUpdateForm updateForm) {
        ResponseDTO<String> checkResult = checkTenderVersionId(tenderVersionId, updateForm.getTenderVersionId());
        if (!checkResult.getOk()) {
            return checkResult;
        }
        return bidTenderService.update(updateForm);
    }

    @Operation(summary = "资源化发布招标文件")
    @PostMapping("/bid/tenders/{tenderVersionId}/actions/publish-tender")
    @SaCheckPermission("bid:tender:publish-tender")
    public ResponseDTO<String> publishResource(@PathVariable Long tenderVersionId, @RequestBody @Valid BidTenderActionForm actionForm) {
        ResponseDTO<String> checkResult = checkTenderVersionId(tenderVersionId, actionForm.getTenderVersionId());
        if (!checkResult.getOk()) {
            return checkResult;
        }
        return bidTenderService.publish(actionForm);
    }

    @Operation(summary = "资源化澄清招标文件")
    @PostMapping("/bid/tenders/{tenderVersionId}/actions/clarify-tender")
    @SaCheckPermission("bid:tender:clarify-tender")
    public ResponseDTO<String> clarifyResource(@PathVariable Long tenderVersionId, @RequestBody @Valid BidTenderActionForm actionForm) {
        ResponseDTO<String> checkResult = checkTenderVersionId(tenderVersionId, actionForm.getTenderVersionId());
        if (!checkResult.getOk()) {
            return checkResult;
        }
        return bidTenderService.clarify(actionForm);
    }

    @Operation(summary = "资源化撤回招标文件")
    @PostMapping("/bid/tenders/{tenderVersionId}/actions/withdraw-tender")
    @SaCheckPermission("bid:tender:withdraw-tender")
    public ResponseDTO<String> withdrawResource(@PathVariable Long tenderVersionId, @RequestBody @Valid BidTenderActionForm actionForm) {
        ResponseDTO<String> checkResult = checkTenderVersionId(tenderVersionId, actionForm.getTenderVersionId());
        if (!checkResult.getOk()) {
            return checkResult;
        }
        return bidTenderService.withdraw(actionForm);
    }

    @Operation(summary = "资源化下载招标文件附件")
    @GetMapping("/bid/tenders/{tenderVersionId}/attachments/{attachmentId}/download")
    @SaCheckPermission("bid:tender:query")
    public void downloadAttachment(@PathVariable Long tenderVersionId,
                                   @PathVariable Long attachmentId,
                                   HttpServletRequest request,
                                   HttpServletResponse response) throws IOException {
        String userAgent = JakartaServletUtil.getHeaderIgnoreCase(request, RequestHeaderConst.USER_AGENT);
        writeDownloadResponse(response, bidTenderService.downloadAttachment(tenderVersionId, attachmentId, userAgent));
    }

    @Operation(summary = "资源化查询招标文件附件预览地址")
    @GetMapping("/bid/tenders/{tenderVersionId}/attachments/{attachmentId}/preview-url")
    @SaCheckPermission("bid:tender:query")
    public ResponseDTO<String> getAttachmentPreviewUrl(@PathVariable Long tenderVersionId, @PathVariable Long attachmentId) {
        return bidTenderService.getAttachmentPreviewUrl(tenderVersionId, attachmentId);
    }

    @Operation(summary = "资源化关联招标文件附件")
    @PostMapping("/bid/tenders/{tenderVersionId}/attachments")
    @SaCheckPermission("bid:tender:update")
    public ResponseDTO<String> createAttachment(@PathVariable Long tenderVersionId, @RequestBody @Valid BidAttachmentCreateForm createForm) {
        return bidTenderService.createAttachment(tenderVersionId, createForm);
    }

    private ResponseDTO<String> checkTenderVersionId(Long pathTenderVersionId, Long formTenderVersionId) {
        if (!Objects.equals(pathTenderVersionId, formTenderVersionId)) {
            return ResponseDTO.userErrorParam("路径招标文件版本ID与表单招标文件版本ID不一致");
        }
        return ResponseDTO.ok();
    }

    private void writeDownloadResponse(HttpServletResponse response, ResponseDTO<FileDownloadVO> downloadResult) throws IOException {
        if (!downloadResult.getOk()) {
            SmartResponseUtil.write(response, downloadResult);
            return;
        }
        FileDownloadVO fileDownloadVO = downloadResult.getData();
        SmartResponseUtil.setDownloadFileHeader(response, fileDownloadVO.getMetadata().getFileName(), fileDownloadVO.getMetadata().getFileSize());
        response.getOutputStream().write(fileDownloadVO.getData());
    }
}
