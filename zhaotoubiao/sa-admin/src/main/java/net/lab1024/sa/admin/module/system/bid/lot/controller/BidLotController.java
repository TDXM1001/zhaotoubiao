package net.lab1024.sa.admin.module.system.bid.lot.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import net.lab1024.sa.admin.constant.AdminSwaggerTagConst;
import net.lab1024.sa.admin.module.system.bid.lot.domain.form.BidLotActionForm;
import net.lab1024.sa.admin.module.system.bid.lot.domain.form.BidLotAddForm;
import net.lab1024.sa.admin.module.system.bid.lot.domain.form.BidLotQueryForm;
import net.lab1024.sa.admin.module.system.bid.lot.domain.form.BidLotUpdateForm;
import net.lab1024.sa.admin.module.system.bid.lot.domain.vo.BidLotVO;
import net.lab1024.sa.admin.module.system.bid.lot.service.BidLotService;
import net.lab1024.sa.base.common.domain.PageResult;
import net.lab1024.sa.base.common.domain.ResponseDTO;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 标段 Controller
 *
 * @author Codex
 * @date 2026-06-07
 */
@RestController
@Tag(name = AdminSwaggerTagConst.System.SYSTEM_BID_LOT)
public class BidLotController {

    @Resource
    private BidLotService bidLotService;

    @Operation(summary = "分页查询标段")
    @PostMapping("/bid/lot/queryPage")
    @SaCheckPermission("bid:lot:query")
    public ResponseDTO<PageResult<BidLotVO>> queryPage(@RequestBody @Valid BidLotQueryForm queryForm) {
        return ResponseDTO.ok(bidLotService.queryPage(queryForm));
    }

    @Operation(summary = "查询标段详情")
    @GetMapping("/bid/lot/get/{lotId}")
    @SaCheckPermission("bid:lot:query")
    public ResponseDTO<BidLotVO> getDetail(@PathVariable Long lotId) {
        return bidLotService.getDetail(lotId);
    }

    @Operation(summary = "查询项目下全部标段")
    @GetMapping("/bid/lot/queryByProjectId/{projectId}")
    @SaCheckPermission("bid:lot:query")
    public ResponseDTO<List<BidLotVO>> queryByProjectId(@PathVariable Long projectId) {
        return bidLotService.queryByProjectId(projectId);
    }

    @Operation(summary = "新增标段")
    @PostMapping("/bid/lot/add")
    @SaCheckPermission("bid:lot:create")
    public ResponseDTO<String> add(@RequestBody @Valid BidLotAddForm addForm) {
        return bidLotService.add(addForm);
    }

    @Operation(summary = "编辑标段")
    @PostMapping("/bid/lot/update")
    @SaCheckPermission("bid:lot:update")
    public ResponseDTO<String> update(@RequestBody @Valid BidLotUpdateForm updateForm) {
        return bidLotService.update(updateForm);
    }

    @Operation(summary = "关闭投标")
    @PostMapping("/bid/lot/closeBid")
    @SaCheckPermission("bid:lot:close-bid")
    public ResponseDTO<String> closeBid(@RequestBody @Valid BidLotActionForm actionForm) {
        return bidLotService.closeBid(actionForm);
    }

    @Operation(summary = "废止标段")
    @PostMapping("/bid/lot/void")
    @SaCheckPermission("bid:lot:void")
    public ResponseDTO<String> voidLot(@RequestBody @Valid BidLotActionForm actionForm) {
        return bidLotService.voidLot(actionForm);
    }
}
