package net.lab1024.sa.admin.module.system.bid.registration.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import net.lab1024.sa.admin.constant.AdminSwaggerTagConst;
import net.lab1024.sa.admin.module.system.bid.registration.domain.form.BidRegistrationActionForm;
import net.lab1024.sa.admin.module.system.bid.registration.domain.form.BidRegistrationAddForm;
import net.lab1024.sa.admin.module.system.bid.registration.domain.form.BidRegistrationQueryForm;
import net.lab1024.sa.admin.module.system.bid.registration.domain.vo.BidRegistrationVO;
import net.lab1024.sa.admin.module.system.bid.registration.service.BidRegistrationService;
import net.lab1024.sa.base.common.domain.PageResult;
import net.lab1024.sa.base.common.domain.ResponseDTO;
import org.springframework.web.bind.annotation.*;

/**
 * 供应商报名 Controller
 *
 * @author Codex
 * @date 2026-06-07
 */
@RestController
@Tag(name = AdminSwaggerTagConst.System.SYSTEM_BID_REGISTRATION)
public class BidRegistrationController {

    @Resource
    private BidRegistrationService bidRegistrationService;

    @Operation(summary = "分页查询供应商报名")
    @PostMapping("/bid/registration/queryPage")
    @SaCheckPermission("bid:registration:query")
    public ResponseDTO<PageResult<BidRegistrationVO>> queryPage(@RequestBody @Valid BidRegistrationQueryForm queryForm) {
        return ResponseDTO.ok(bidRegistrationService.queryPage(queryForm));
    }

    @Operation(summary = "查询供应商报名详情")
    @GetMapping("/bid/registration/get/{registrationId}")
    @SaCheckPermission("bid:registration:query")
    public ResponseDTO<BidRegistrationVO> getDetail(@PathVariable Long registrationId) {
        return bidRegistrationService.getDetail(registrationId);
    }

    @Operation(summary = "新增供应商报名")
    @PostMapping("/bid/registration/add")
    @SaCheckPermission("bid:registration:create")
    public ResponseDTO<String> add(@RequestBody @Valid BidRegistrationAddForm addForm) {
        return bidRegistrationService.add(addForm);
    }

    @Operation(summary = "报名审核通过")
    @PostMapping("/bid/registration/approve")
    @SaCheckPermission("bid:registration:approve")
    public ResponseDTO<String> approve(@RequestBody @Valid BidRegistrationActionForm actionForm) {
        return bidRegistrationService.approve(actionForm);
    }

    @Operation(summary = "报名驳回")
    @PostMapping("/bid/registration/reject")
    @SaCheckPermission("bid:registration:reject")
    public ResponseDTO<String> reject(@RequestBody @Valid BidRegistrationActionForm actionForm) {
        return bidRegistrationService.reject(actionForm);
    }

    @Operation(summary = "取消报名")
    @PostMapping("/bid/registration/cancel")
    @SaCheckPermission("bid:registration:cancel")
    public ResponseDTO<String> cancel(@RequestBody @Valid BidRegistrationActionForm actionForm) {
        return bidRegistrationService.cancel(actionForm);
    }
}
