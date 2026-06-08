package net.lab1024.sa.admin.module.system.bid.award.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import net.lab1024.sa.admin.constant.AdminSwaggerTagConst;
import net.lab1024.sa.admin.module.system.bid.award.domain.form.BidAwardActionForm;
import net.lab1024.sa.admin.module.system.bid.award.domain.form.BidAwardCreateForm;
import net.lab1024.sa.admin.module.system.bid.award.domain.form.BidAwardQueryForm;
import net.lab1024.sa.admin.module.system.bid.award.domain.vo.BidAwardVO;
import net.lab1024.sa.admin.module.system.bid.award.service.BidAwardService;
import net.lab1024.sa.base.common.domain.PageResult;
import net.lab1024.sa.base.common.domain.ResponseDTO;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

/**
 * 定标 Controller
 *
 * @author Codex
 * @date 2026-06-08
 */
@RestController
@Tag(name = AdminSwaggerTagConst.System.SYSTEM_BID_AWARD)
public class BidAwardController {

    @Resource
    private BidAwardService bidAwardService;

    @Operation(summary = "资源化分页查询定标")
    @PostMapping("/bid/awards/search")
    @SaCheckPermission("bid:award:query")
    public ResponseDTO<PageResult<BidAwardVO>> search(@RequestBody @Valid BidAwardQueryForm queryForm) {
        return ResponseDTO.ok(bidAwardService.queryPage(queryForm));
    }

    @Operation(summary = "资源化查询定标详情")
    @GetMapping("/bid/awards/{awardId}")
    @SaCheckPermission("bid:award:query")
    public ResponseDTO<BidAwardVO> getDetail(@PathVariable Long awardId) {
        return bidAwardService.getDetail(awardId);
    }

    @Operation(summary = "资源化查询标段定标记录")
    @GetMapping("/bid/lots/{lotId}/award")
    @SaCheckPermission("bid:award:query")
    public ResponseDTO<BidAwardVO> getByLotId(@PathVariable Long lotId) {
        return bidAwardService.getByLotId(lotId);
    }

    @Operation(summary = "资源化新增定标记录")
    @PostMapping("/bid/awards")
    @SaCheckPermission("bid:award:create")
    public ResponseDTO<String> create(@RequestBody @Valid BidAwardCreateForm createForm) {
        return bidAwardService.create(createForm);
    }

    @Operation(summary = "资源化确认定标")
    @PostMapping("/bid/awards/{awardId}/actions/confirm-award")
    @SaCheckPermission("bid:award:confirm-award")
    public ResponseDTO<String> confirmResource(@PathVariable Long awardId, @RequestBody @Valid BidAwardActionForm actionForm) {
        ResponseDTO<String> checkResult = checkAwardId(awardId, actionForm.getAwardId());
        if (!checkResult.getOk()) {
            return checkResult;
        }
        return bidAwardService.confirm(actionForm);
    }

    @Operation(summary = "资源化回退定标")
    @PostMapping("/bid/awards/{awardId}/actions/rollback-award")
    @SaCheckPermission("bid:award:rollback-award")
    public ResponseDTO<String> rollbackResource(@PathVariable Long awardId, @RequestBody @Valid BidAwardActionForm actionForm) {
        ResponseDTO<String> checkResult = checkAwardId(awardId, actionForm.getAwardId());
        if (!checkResult.getOk()) {
            return checkResult;
        }
        return bidAwardService.rollback(actionForm);
    }

    @Operation(summary = "资源化取消定标")
    @PostMapping("/bid/awards/{awardId}/actions/cancel-award")
    @SaCheckPermission("bid:award:cancel-award")
    public ResponseDTO<String> cancelResource(@PathVariable Long awardId, @RequestBody @Valid BidAwardActionForm actionForm) {
        ResponseDTO<String> checkResult = checkAwardId(awardId, actionForm.getAwardId());
        if (!checkResult.getOk()) {
            return checkResult;
        }
        return bidAwardService.cancel(actionForm);
    }

    private ResponseDTO<String> checkAwardId(Long pathAwardId, Long formAwardId) {
        if (!Objects.equals(pathAwardId, formAwardId)) {
            return ResponseDTO.userErrorParam("路径定标ID与表单定标ID不一致");
        }
        return ResponseDTO.ok();
    }
}
