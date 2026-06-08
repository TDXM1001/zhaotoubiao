package net.lab1024.sa.admin.module.system.bid.evaluation.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import net.lab1024.sa.admin.constant.AdminSwaggerTagConst;
import net.lab1024.sa.admin.module.system.bid.evaluation.domain.form.BidEvaluationActionForm;
import net.lab1024.sa.admin.module.system.bid.evaluation.domain.form.BidEvaluationCreateForm;
import net.lab1024.sa.admin.module.system.bid.evaluation.domain.form.BidEvaluationQueryForm;
import net.lab1024.sa.admin.module.system.bid.evaluation.domain.vo.BidEvaluationVO;
import net.lab1024.sa.admin.module.system.bid.evaluation.service.BidEvaluationService;
import net.lab1024.sa.base.common.domain.PageResult;
import net.lab1024.sa.base.common.domain.ResponseDTO;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

/**
 * 评标 Controller
 *
 * @author Codex
 * @date 2026-06-08
 */
@RestController
@Tag(name = AdminSwaggerTagConst.System.SYSTEM_BID_EVALUATION)
public class BidEvaluationController {

    @Resource
    private BidEvaluationService bidEvaluationService;

    @Operation(summary = "资源化分页查询评标")
    @PostMapping("/bid/evaluations/search")
    @SaCheckPermission("bid:evaluation:query")
    public ResponseDTO<PageResult<BidEvaluationVO>> search(@RequestBody @Valid BidEvaluationQueryForm queryForm) {
        return ResponseDTO.ok(bidEvaluationService.queryPage(queryForm));
    }

    @Operation(summary = "资源化查询评标详情")
    @GetMapping("/bid/evaluations/{evaluationId}")
    @SaCheckPermission("bid:evaluation:query")
    public ResponseDTO<BidEvaluationVO> getDetail(@PathVariable Long evaluationId) {
        return bidEvaluationService.getDetail(evaluationId);
    }

    @Operation(summary = "资源化查询标段评标记录")
    @GetMapping("/bid/lots/{lotId}/evaluation")
    @SaCheckPermission("bid:evaluation:query")
    public ResponseDTO<BidEvaluationVO> getByLotId(@PathVariable Long lotId) {
        return bidEvaluationService.getByLotId(lotId);
    }

    @Operation(summary = "资源化新增评标记录")
    @PostMapping("/bid/evaluations")
    @SaCheckPermission("bid:evaluation:create")
    public ResponseDTO<String> create(@RequestBody @Valid BidEvaluationCreateForm createForm) {
        return bidEvaluationService.create(createForm);
    }

    @Operation(summary = "资源化开始评标")
    @PostMapping("/bid/evaluations/{evaluationId}/actions/start-evaluation")
    @SaCheckPermission("bid:evaluation:start-evaluation")
    public ResponseDTO<String> startResource(@PathVariable Long evaluationId, @RequestBody @Valid BidEvaluationActionForm actionForm) {
        ResponseDTO<String> checkResult = checkEvaluationId(evaluationId, actionForm.getEvaluationId());
        if (!checkResult.getOk()) {
            return checkResult;
        }
        return bidEvaluationService.start(actionForm);
    }

    @Operation(summary = "资源化评标定稿")
    @PostMapping("/bid/evaluations/{evaluationId}/actions/finalize-evaluation")
    @SaCheckPermission("bid:evaluation:finalize-evaluation")
    public ResponseDTO<String> finalizeResource(@PathVariable Long evaluationId, @RequestBody @Valid BidEvaluationActionForm actionForm) {
        ResponseDTO<String> checkResult = checkEvaluationId(evaluationId, actionForm.getEvaluationId());
        if (!checkResult.getOk()) {
            return checkResult;
        }
        return bidEvaluationService.finalizeEvaluation(actionForm);
    }

    @Operation(summary = "资源化回退评标")
    @PostMapping("/bid/evaluations/{evaluationId}/actions/rollback-evaluation")
    @SaCheckPermission("bid:evaluation:rollback-evaluation")
    public ResponseDTO<String> rollbackResource(@PathVariable Long evaluationId, @RequestBody @Valid BidEvaluationActionForm actionForm) {
        ResponseDTO<String> checkResult = checkEvaluationId(evaluationId, actionForm.getEvaluationId());
        if (!checkResult.getOk()) {
            return checkResult;
        }
        return bidEvaluationService.rollback(actionForm);
    }

    private ResponseDTO<String> checkEvaluationId(Long pathEvaluationId, Long formEvaluationId) {
        if (!Objects.equals(pathEvaluationId, formEvaluationId)) {
            return ResponseDTO.userErrorParam("路径评标ID与表单评标ID不一致");
        }
        return ResponseDTO.ok();
    }
}
