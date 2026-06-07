package net.lab1024.sa.admin.module.system.bid.project.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import net.lab1024.sa.admin.constant.AdminSwaggerTagConst;
import net.lab1024.sa.admin.module.system.bid.project.domain.form.BidProjectActionForm;
import net.lab1024.sa.admin.module.system.bid.project.domain.form.BidProjectAddForm;
import net.lab1024.sa.admin.module.system.bid.project.domain.form.BidProjectQueryForm;
import net.lab1024.sa.admin.module.system.bid.project.domain.form.BidProjectUpdateForm;
import net.lab1024.sa.admin.module.system.bid.project.domain.vo.BidProjectOptionVO;
import net.lab1024.sa.admin.module.system.bid.project.domain.vo.BidProjectVO;
import net.lab1024.sa.admin.module.system.bid.project.service.BidProjectService;
import net.lab1024.sa.base.common.domain.PageResult;
import net.lab1024.sa.base.common.domain.ResponseDTO;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 招标项目 Controller
 *
 * @author Codex
 * @date 2026-06-07
 */
@RestController
@Tag(name = AdminSwaggerTagConst.System.SYSTEM_BID_PROJECT)
public class BidProjectController {

    @Resource
    private BidProjectService bidProjectService;

    @Operation(summary = "分页查询招标项目")
    @PostMapping("/bid/project/queryPage")
    @SaCheckPermission("bid:project:query")
    public ResponseDTO<PageResult<BidProjectVO>> queryPage(@RequestBody @Valid BidProjectQueryForm queryForm) {
        return ResponseDTO.ok(bidProjectService.queryPage(queryForm));
    }

    @Operation(summary = "查询招标项目详情")
    @GetMapping("/bid/project/get/{projectId}")
    @SaCheckPermission("bid:project:query")
    public ResponseDTO<BidProjectVO> getDetail(@PathVariable Long projectId) {
        return bidProjectService.getDetail(projectId);
    }

    @Operation(summary = "查询招标项目下拉")
    @GetMapping("/bid/project/queryList")
    @SaCheckPermission("bid:project:query")
    public ResponseDTO<List<BidProjectOptionVO>> queryList(@RequestParam(value = "status", required = false) String status) {
        return bidProjectService.queryList(status);
    }

    @Operation(summary = "新增招标项目")
    @PostMapping("/bid/project/add")
    @SaCheckPermission("bid:project:create")
    public ResponseDTO<String> add(@RequestBody @Valid BidProjectAddForm addForm) {
        return bidProjectService.add(addForm);
    }

    @Operation(summary = "编辑招标项目")
    @PostMapping("/bid/project/update")
    @SaCheckPermission("bid:project:update")
    public ResponseDTO<String> update(@RequestBody @Valid BidProjectUpdateForm updateForm) {
        return bidProjectService.update(updateForm);
    }

    @Operation(summary = "提交招标项目计划")
    @PostMapping("/bid/project/submitPlan")
    @SaCheckPermission("bid:project:submit-plan")
    public ResponseDTO<String> submitPlan(@RequestBody @Valid BidProjectActionForm actionForm) {
        return bidProjectService.submitPlan(actionForm);
    }

    @Operation(summary = "发布招标项目")
    @PostMapping("/bid/project/publish")
    @SaCheckPermission("bid:project:publish")
    public ResponseDTO<String> publish(@RequestBody @Valid BidProjectActionForm actionForm) {
        return bidProjectService.publish(actionForm);
    }

    @Operation(summary = "作废招标项目")
    @PostMapping("/bid/project/cancel")
    @SaCheckPermission("bid:project:cancel")
    public ResponseDTO<String> cancel(@RequestBody @Valid BidProjectActionForm actionForm) {
        return bidProjectService.cancel(actionForm);
    }
}
