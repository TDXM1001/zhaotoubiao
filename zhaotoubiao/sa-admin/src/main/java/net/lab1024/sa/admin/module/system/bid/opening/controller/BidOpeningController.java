package net.lab1024.sa.admin.module.system.bid.opening.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import net.lab1024.sa.admin.constant.AdminSwaggerTagConst;
import net.lab1024.sa.admin.module.system.bid.opening.domain.form.BidOpeningActionForm;
import net.lab1024.sa.admin.module.system.bid.opening.domain.form.BidOpeningCreateForm;
import net.lab1024.sa.admin.module.system.bid.opening.domain.form.BidOpeningQueryForm;
import net.lab1024.sa.admin.module.system.bid.opening.domain.vo.BidOpeningVO;
import net.lab1024.sa.admin.module.system.bid.opening.service.BidOpeningService;
import net.lab1024.sa.base.common.domain.PageResult;
import net.lab1024.sa.base.common.domain.ResponseDTO;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

/**
 * 开标 Controller
 *
 * @author Codex
 * @date 2026-06-08
 */
@RestController
@Tag(name = AdminSwaggerTagConst.System.SYSTEM_BID_OPENING)
public class BidOpeningController {

    @Resource
    private BidOpeningService bidOpeningService;

    @Operation(summary = "资源化分页查询开标")
    @PostMapping("/bid/openings/search")
    @SaCheckPermission("bid:opening:query")
    public ResponseDTO<PageResult<BidOpeningVO>> search(@RequestBody @Valid BidOpeningQueryForm queryForm) {
        return ResponseDTO.ok(bidOpeningService.queryPage(queryForm));
    }

    @Operation(summary = "资源化查询开标详情")
    @GetMapping("/bid/openings/{openingId}")
    @SaCheckPermission("bid:opening:query")
    public ResponseDTO<BidOpeningVO> getDetail(@PathVariable Long openingId) {
        return bidOpeningService.getDetail(openingId);
    }

    @Operation(summary = "资源化查询标段开标记录")
    @GetMapping("/bid/lots/{lotId}/opening")
    @SaCheckPermission("bid:opening:query")
    public ResponseDTO<BidOpeningVO> getByLotId(@PathVariable Long lotId) {
        return bidOpeningService.getByLotId(lotId);
    }

    @Operation(summary = "资源化新增开标记录")
    @PostMapping("/bid/openings")
    @SaCheckPermission("bid:opening:create")
    public ResponseDTO<String> create(@RequestBody @Valid BidOpeningCreateForm createForm) {
        return bidOpeningService.create(createForm);
    }

    @Operation(summary = "资源化开始开标")
    @PostMapping("/bid/openings/{openingId}/actions/start-opening")
    @SaCheckPermission("bid:opening:start-opening")
    public ResponseDTO<String> startResource(@PathVariable Long openingId, @RequestBody @Valid BidOpeningActionForm actionForm) {
        ResponseDTO<String> checkResult = checkOpeningId(openingId, actionForm.getOpeningId());
        if (!checkResult.getOk()) {
            return checkResult;
        }
        return bidOpeningService.start(actionForm);
    }

    @Operation(summary = "资源化完成开标")
    @PostMapping("/bid/openings/{openingId}/actions/complete-opening")
    @SaCheckPermission("bid:opening:complete-opening")
    public ResponseDTO<String> completeResource(@PathVariable Long openingId, @RequestBody @Valid BidOpeningActionForm actionForm) {
        ResponseDTO<String> checkResult = checkOpeningId(openingId, actionForm.getOpeningId());
        if (!checkResult.getOk()) {
            return checkResult;
        }
        return bidOpeningService.complete(actionForm);
    }

    @Operation(summary = "资源化异常关闭开标")
    @PostMapping("/bid/openings/{openingId}/actions/abnormal-close-opening")
    @SaCheckPermission("bid:opening:abnormal-close-opening")
    public ResponseDTO<String> abnormalCloseResource(@PathVariable Long openingId, @RequestBody @Valid BidOpeningActionForm actionForm) {
        ResponseDTO<String> checkResult = checkOpeningId(openingId, actionForm.getOpeningId());
        if (!checkResult.getOk()) {
            return checkResult;
        }
        return bidOpeningService.abnormalClose(actionForm);
    }

    private ResponseDTO<String> checkOpeningId(Long pathOpeningId, Long formOpeningId) {
        if (!Objects.equals(pathOpeningId, formOpeningId)) {
            return ResponseDTO.userErrorParam("路径开标ID与表单开标ID不一致");
        }
        return ResponseDTO.ok();
    }
}
