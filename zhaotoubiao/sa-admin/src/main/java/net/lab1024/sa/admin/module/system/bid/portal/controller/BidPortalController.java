package net.lab1024.sa.admin.module.system.bid.portal.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import net.lab1024.sa.admin.module.system.bid.portal.domain.form.BidPortalAuthLoginForm;
import net.lab1024.sa.admin.module.system.bid.portal.domain.form.BidPortalAuthRegisterForm;
import net.lab1024.sa.admin.module.system.bid.portal.domain.form.BidPortalProjectQueryForm;
import net.lab1024.sa.admin.module.system.bid.portal.domain.form.BidPortalRegistrationCreateForm;
import net.lab1024.sa.admin.module.system.bid.portal.domain.form.BidPortalSubmissionActionForm;
import net.lab1024.sa.admin.module.system.bid.portal.domain.form.BidPortalSubmissionCreateForm;
import net.lab1024.sa.admin.module.system.bid.portal.domain.form.BidPortalSubmissionSubmitForm;
import net.lab1024.sa.admin.module.system.bid.portal.domain.vo.BidPortalAuthVO;
import net.lab1024.sa.admin.module.system.bid.portal.domain.vo.BidPortalProjectVO;
import net.lab1024.sa.admin.module.system.bid.portal.domain.vo.BidPortalRegistrationVO;
import net.lab1024.sa.admin.module.system.bid.portal.domain.vo.BidPortalRequestUser;
import net.lab1024.sa.admin.module.system.bid.portal.domain.vo.BidPortalResultVO;
import net.lab1024.sa.admin.module.system.bid.portal.domain.vo.BidPortalSubmissionVO;
import net.lab1024.sa.admin.module.system.bid.portal.service.BidPortalAuthService;
import net.lab1024.sa.admin.module.system.bid.portal.service.BidPortalService;
import net.lab1024.sa.admin.module.system.bid.portal.util.BidPortalRequestUtil;
import net.lab1024.sa.base.common.annoation.NoNeedLogin;
import net.lab1024.sa.base.common.domain.PageResult;
import net.lab1024.sa.base.common.domain.ResponseDTO;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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

    @Resource
    private BidPortalAuthService bidPortalAuthService;

    @NoNeedLogin
    @Operation(summary = "门户注册")
    @PostMapping("/bid/portal/auth/register")
    public ResponseDTO<BidPortalAuthVO> register(@RequestBody @Valid BidPortalAuthRegisterForm registerForm) {
        return bidPortalAuthService.register(registerForm);
    }

    @NoNeedLogin
    @Operation(summary = "门户登录")
    @PostMapping("/bid/portal/auth/login")
    public ResponseDTO<BidPortalAuthVO> login(@RequestBody @Valid BidPortalAuthLoginForm loginForm) {
        return bidPortalAuthService.login(loginForm);
    }

    @Operation(summary = "门户退出登录")
    @PostMapping("/bid/portal/auth/logout")
    public ResponseDTO<String> logout() {
        return bidPortalAuthService.logout();
    }

    @Operation(summary = "门户当前登录供应商")
    @GetMapping("/bid/portal/auth/me")
    public ResponseDTO<BidPortalAuthVO> getCurrentSupplier() {
        return bidPortalAuthService.getCurrent(BidPortalRequestUtil.getRequestUser());
    }

    @NoNeedLogin
    @Operation(summary = "门户分页查询可见项目")
    @PostMapping("/bid/portal/projects/search")
    public ResponseDTO<PageResult<BidPortalProjectVO>> searchProjects(@RequestBody @Valid BidPortalProjectQueryForm queryForm) {
        return ResponseDTO.ok(bidPortalService.queryProjectPage(queryForm));
    }

    @NoNeedLogin
    @Operation(summary = "门户查询项目详情")
    @GetMapping("/bid/portal/projects/{projectId}")
    public ResponseDTO<BidPortalProjectVO> getProjectDetail(@PathVariable Long projectId) {
        return bidPortalService.getProjectDetail(projectId);
    }

    @Operation(summary = "门户提交报名")
    @PostMapping("/bid/portal/registrations")
    public ResponseDTO<String> createRegistration(@RequestBody @Valid BidPortalRegistrationCreateForm createForm) {
        return bidPortalService.createRegistration(createForm, currentSupplier());
    }

    @Operation(summary = "门户查询本人报名详情")
    @GetMapping("/bid/portal/registrations/{registrationId}")
    public ResponseDTO<BidPortalRegistrationVO> getRegistration(@PathVariable Long registrationId) {
        return bidPortalService.getRegistration(registrationId, currentSupplier());
    }

    @Operation(summary = "门户创建投标主记录")
    @PostMapping("/bid/portal/submissions")
    public ResponseDTO<String> createSubmission(@RequestBody @Valid BidPortalSubmissionCreateForm createForm) {
        return bidPortalService.createSubmission(createForm, currentSupplier());
    }

    @Operation(summary = "门户查询本人投标详情")
    @GetMapping("/bid/portal/submissions/{submissionId}")
    public ResponseDTO<BidPortalSubmissionVO> getSubmission(@PathVariable Long submissionId) {
        return bidPortalService.getSubmission(submissionId, currentSupplier());
    }

    @Operation(summary = "门户提交投标")
    @PostMapping("/bid/portal/submissions/{submissionId}/actions/submit-bid")
    public ResponseDTO<String> submitBid(@PathVariable Long submissionId, @RequestBody @Valid BidPortalSubmissionSubmitForm submitForm) {
        ResponseDTO<String> checkResult = checkSubmissionId(submissionId, submitForm.getSubmissionId());
        if (!checkResult.getOk()) {
            return checkResult;
        }
        return bidPortalService.submitBid(submitForm, currentSupplier());
    }

    @Operation(summary = "门户撤回投标")
    @PostMapping("/bid/portal/submissions/{submissionId}/actions/withdraw-bid")
    public ResponseDTO<String> withdrawBid(@PathVariable Long submissionId, @RequestBody @Valid BidPortalSubmissionActionForm actionForm) {
        ResponseDTO<String> checkResult = checkSubmissionId(submissionId, actionForm.getSubmissionId());
        if (!checkResult.getOk()) {
            return checkResult;
        }
        return bidPortalService.withdrawBid(actionForm, currentSupplier());
    }

    @Operation(summary = "门户查询本人标段结果")
    @GetMapping("/bid/portal/lots/{lotId}/result")
    public ResponseDTO<BidPortalResultVO> getLotResult(@PathVariable Long lotId) {
        return bidPortalService.getLotResult(lotId, currentSupplier());
    }

    private ResponseDTO<String> checkSubmissionId(Long pathSubmissionId, Long formSubmissionId) {
        if (!Objects.equals(pathSubmissionId, formSubmissionId)) {
            return ResponseDTO.userErrorParam("路径投标ID与表单投标ID不一致");
        }
        return ResponseDTO.ok();
    }

    private BidPortalRequestUser currentSupplier() {
        return BidPortalRequestUtil.getRequestUser();
    }
}
