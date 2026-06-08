package net.lab1024.sa.admin.module.system.bid.portal.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import net.lab1024.sa.admin.module.system.bid.portal.domain.form.BidPortalProjectQueryForm;
import net.lab1024.sa.admin.module.system.bid.portal.domain.form.BidPortalRegistrationCreateForm;
import net.lab1024.sa.admin.module.system.bid.portal.domain.form.BidPortalSubmissionActionForm;
import net.lab1024.sa.admin.module.system.bid.portal.domain.form.BidPortalSubmissionCreateForm;
import net.lab1024.sa.admin.module.system.bid.portal.domain.form.BidPortalSubmissionSubmitForm;
import net.lab1024.sa.admin.module.system.bid.portal.domain.vo.BidPortalProjectVO;
import net.lab1024.sa.admin.module.system.bid.portal.domain.vo.BidPortalRegistrationVO;
import net.lab1024.sa.admin.module.system.bid.portal.domain.vo.BidPortalSubmissionVO;
import net.lab1024.sa.admin.module.system.bid.portal.service.BidPortalService;
import net.lab1024.sa.base.common.domain.PageResult;
import net.lab1024.sa.base.common.domain.ResponseDTO;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

/**
 * 供应商门户 Controller
 *
 * @author Codex
 * @date 2026-06-08
 */
@Validated
@RestController
@Tag(name = "供应商门户")
public class BidPortalController {

    @Resource
    private BidPortalService bidPortalService;

    @Operation(summary = "门户分页查询可见项目")
    @PostMapping("/bid/portal/projects/search")
    public ResponseDTO<PageResult<BidPortalProjectVO>> searchProjects(@RequestBody @Valid BidPortalProjectQueryForm queryForm) {
        return ResponseDTO.ok(bidPortalService.queryProjectPage(queryForm));
    }

    @Operation(summary = "门户查询项目详情")
    @GetMapping("/bid/portal/projects/{projectId}")
    public ResponseDTO<BidPortalProjectVO> getProjectDetail(@PathVariable Long projectId) {
        return bidPortalService.getProjectDetail(projectId);
    }

    @Operation(summary = "门户提交报名")
    @PostMapping("/bid/portal/registrations")
    public ResponseDTO<String> createRegistration(@RequestBody @Valid BidPortalRegistrationCreateForm createForm) {
        return bidPortalService.createRegistration(createForm);
    }

    @Operation(summary = "门户查询本人报名详情")
    @GetMapping("/bid/portal/registrations/{registrationId}")
    public ResponseDTO<BidPortalRegistrationVO> getRegistration(@PathVariable Long registrationId,
                                                                @RequestParam @NotBlank(message = "统一社会信用代码不能为空") String supplierCreditCode) {
        return bidPortalService.getRegistration(registrationId, supplierCreditCode);
    }

    @Operation(summary = "门户创建投标主记录")
    @PostMapping("/bid/portal/submissions")
    public ResponseDTO<String> createSubmission(@RequestBody @Valid BidPortalSubmissionCreateForm createForm) {
        return bidPortalService.createSubmission(createForm);
    }

    @Operation(summary = "门户查询本人投标详情")
    @GetMapping("/bid/portal/submissions/{submissionId}")
    public ResponseDTO<BidPortalSubmissionVO> getSubmission(@PathVariable Long submissionId,
                                                            @RequestParam @NotBlank(message = "统一社会信用代码不能为空") String supplierCreditCode) {
        return bidPortalService.getSubmission(submissionId, supplierCreditCode);
    }

    @Operation(summary = "门户提交投标")
    @PostMapping("/bid/portal/submissions/{submissionId}/actions/submit-bid")
    public ResponseDTO<String> submitBid(@PathVariable Long submissionId, @RequestBody @Valid BidPortalSubmissionSubmitForm submitForm) {
        ResponseDTO<String> checkResult = checkSubmissionId(submissionId, submitForm.getSubmissionId());
        if (!checkResult.getOk()) {
            return checkResult;
        }
        return bidPortalService.submitBid(submitForm);
    }

    @Operation(summary = "门户撤回投标")
    @PostMapping("/bid/portal/submissions/{submissionId}/actions/withdraw-bid")
    public ResponseDTO<String> withdrawBid(@PathVariable Long submissionId, @RequestBody @Valid BidPortalSubmissionActionForm actionForm) {
        ResponseDTO<String> checkResult = checkSubmissionId(submissionId, actionForm.getSubmissionId());
        if (!checkResult.getOk()) {
            return checkResult;
        }
        return bidPortalService.withdrawBid(actionForm);
    }

    private ResponseDTO<String> checkSubmissionId(Long pathSubmissionId, Long formSubmissionId) {
        if (!Objects.equals(pathSubmissionId, formSubmissionId)) {
            return ResponseDTO.userErrorParam("路径投标ID与表单投标ID不一致");
        }
        return ResponseDTO.ok();
    }
}
