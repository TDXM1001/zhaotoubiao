package net.lab1024.sa.admin.module.system.bid.submission.controller;

import cn.hutool.extra.servlet.JakartaServletUtil;
import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import net.lab1024.sa.admin.constant.AdminSwaggerTagConst;
import net.lab1024.sa.admin.module.system.bid.submission.domain.form.BidSubmissionActionForm;
import net.lab1024.sa.admin.module.system.bid.submission.domain.form.BidSubmissionCreateForm;
import net.lab1024.sa.admin.module.system.bid.submission.domain.form.BidSubmissionQueryForm;
import net.lab1024.sa.admin.module.system.bid.submission.domain.form.BidSubmissionSubmitForm;
import net.lab1024.sa.admin.module.system.bid.submission.domain.vo.BidSubmissionVO;
import net.lab1024.sa.admin.module.system.bid.submission.service.BidSubmissionService;
import net.lab1024.sa.base.common.constant.RequestHeaderConst;
import net.lab1024.sa.base.common.domain.PageResult;
import net.lab1024.sa.base.common.domain.ResponseDTO;
import net.lab1024.sa.base.common.util.SmartResponseUtil;
import net.lab1024.sa.base.module.support.file.domain.vo.FileDownloadVO;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Objects;

/**
 * 投标提交 Controller
 *
 * @author Codex
 * @date 2026-06-08
 */
@RestController
@Tag(name = AdminSwaggerTagConst.System.SYSTEM_BID_SUBMISSION)
public class BidSubmissionController {

    @Resource
    private BidSubmissionService bidSubmissionService;

    @Operation(summary = "资源化分页查询投标")
    @PostMapping("/bid/submissions/search")
    @SaCheckPermission("bid:submission:query")
    public ResponseDTO<PageResult<BidSubmissionVO>> search(@RequestBody @Valid BidSubmissionQueryForm queryForm) {
        return ResponseDTO.ok(bidSubmissionService.queryPage(queryForm));
    }

    @Operation(summary = "资源化查询投标详情")
    @GetMapping("/bid/submissions/{submissionId}")
    @SaCheckPermission("bid:submission:query")
    public ResponseDTO<BidSubmissionVO> getDetail(@PathVariable Long submissionId) {
        return bidSubmissionService.getDetail(submissionId);
    }

    @Operation(summary = "资源化按报名查询投标")
    @GetMapping("/bid/registrations/{registrationId}/submission")
    @SaCheckPermission("bid:submission:query")
    public ResponseDTO<BidSubmissionVO> getByRegistrationId(@PathVariable Long registrationId) {
        return bidSubmissionService.getByRegistrationId(registrationId);
    }

    @Operation(summary = "资源化新增投标主记录")
    @PostMapping("/bid/submissions")
    @SaCheckPermission("bid:submission:create")
    public ResponseDTO<String> create(@RequestBody @Valid BidSubmissionCreateForm createForm) {
        return bidSubmissionService.create(createForm);
    }

    @Operation(summary = "资源化提交投标")
    @PostMapping("/bid/submissions/{submissionId}/actions/submit-bid")
    @SaCheckPermission("bid:submission:submit-bid")
    public ResponseDTO<String> submitResource(@PathVariable Long submissionId, @RequestBody @Valid BidSubmissionSubmitForm submitForm) {
        ResponseDTO<String> checkResult = checkSubmissionId(submissionId, submitForm.getSubmissionId());
        if (!checkResult.getOk()) {
            return checkResult;
        }
        return bidSubmissionService.submitBid(submitForm);
    }

    @Operation(summary = "资源化撤回投标")
    @PostMapping("/bid/submissions/{submissionId}/actions/withdraw-bid")
    @SaCheckPermission("bid:submission:withdraw-bid")
    public ResponseDTO<String> withdrawResource(@PathVariable Long submissionId, @RequestBody @Valid BidSubmissionActionForm actionForm) {
        ResponseDTO<String> checkResult = checkSubmissionId(submissionId, actionForm.getSubmissionId());
        if (!checkResult.getOk()) {
            return checkResult;
        }
        return bidSubmissionService.withdraw(actionForm);
    }

    @Operation(summary = "资源化下载投标附件")
    @GetMapping("/bid/submissions/{submissionId}/attachments/{attachmentId}/download")
    @SaCheckPermission("bid:submission:download")
    public void downloadAttachment(@PathVariable Long submissionId,
                                   @PathVariable Long attachmentId,
                                   HttpServletRequest request,
                                   HttpServletResponse response) throws IOException {
        String userAgent = JakartaServletUtil.getHeaderIgnoreCase(request, RequestHeaderConst.USER_AGENT);
        writeDownloadResponse(response, bidSubmissionService.downloadAttachment(submissionId, attachmentId, userAgent));
    }

    @Operation(summary = "资源化查询投标附件预览地址")
    @GetMapping("/bid/submissions/{submissionId}/attachments/{attachmentId}/preview-url")
    @SaCheckPermission("bid:submission:download")
    public ResponseDTO<String> getAttachmentPreviewUrl(@PathVariable Long submissionId, @PathVariable Long attachmentId) {
        return bidSubmissionService.getAttachmentPreviewUrl(submissionId, attachmentId);
    }

    private ResponseDTO<String> checkSubmissionId(Long pathSubmissionId, Long formSubmissionId) {
        if (!Objects.equals(pathSubmissionId, formSubmissionId)) {
            return ResponseDTO.userErrorParam("路径投标ID与表单投标ID不一致");
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
