package net.lab1024.sa.admin.module.system.bid.project.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import net.lab1024.sa.admin.module.system.bid.common.constant.BidLotStatusEnum;
import net.lab1024.sa.admin.module.system.bid.common.constant.BidProjectStatusEnum;
import net.lab1024.sa.admin.module.system.bid.common.service.BidWorkflowHistoryService;
import net.lab1024.sa.admin.module.system.bid.lot.dao.BidLotDao;
import net.lab1024.sa.admin.module.system.bid.project.dao.BidProjectDao;
import net.lab1024.sa.admin.module.system.bid.project.domain.entity.BidProjectEntity;
import net.lab1024.sa.admin.module.system.bid.project.domain.form.BidProjectActionForm;
import net.lab1024.sa.admin.module.system.bid.project.domain.form.BidProjectAddForm;
import net.lab1024.sa.admin.module.system.bid.project.domain.form.BidProjectQueryForm;
import net.lab1024.sa.admin.module.system.bid.project.domain.form.BidProjectUpdateForm;
import net.lab1024.sa.admin.module.system.bid.project.domain.vo.BidProjectOptionVO;
import net.lab1024.sa.admin.module.system.bid.project.domain.vo.BidProjectVO;
import net.lab1024.sa.admin.module.system.department.dao.DepartmentDao;
import net.lab1024.sa.admin.module.system.department.domain.entity.DepartmentEntity;
import net.lab1024.sa.admin.module.system.employee.dao.EmployeeDao;
import net.lab1024.sa.admin.module.system.employee.domain.entity.EmployeeEntity;
import net.lab1024.sa.base.common.code.UserErrorCode;
import net.lab1024.sa.base.common.domain.PageResult;
import net.lab1024.sa.base.common.domain.RequestUser;
import net.lab1024.sa.base.common.domain.ResponseDTO;
import net.lab1024.sa.base.common.util.SmartBeanUtil;
import net.lab1024.sa.base.common.util.SmartPageUtil;
import net.lab1024.sa.base.common.util.SmartRequestUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 招标项目 Service
 *
 * @author Codex
 * @date 2026-06-07
 */
@Service
public class BidProjectService {

    @Resource
    private BidProjectDao bidProjectDao;

    @Resource
    private BidLotDao bidLotDao;

    @Resource
    private DepartmentDao departmentDao;

    @Resource
    private EmployeeDao employeeDao;

    @Resource
    private BidWorkflowHistoryService bidWorkflowHistoryService;

    /**
     * 分页查询
     */
    public PageResult<BidProjectVO> queryPage(BidProjectQueryForm queryForm) {
        queryForm.setDeletedFlag(Boolean.FALSE);
        Page<?> page = SmartPageUtil.convert2PageQuery(queryForm);
        List<BidProjectVO> list = bidProjectDao.queryPage(page, queryForm);
        if (list != null) {
            list.forEach(this::fillAllowedActions);
        }
        return SmartPageUtil.convert2PageResult(page, list);
    }

    /**
     * 查询详情
     */
    public ResponseDTO<BidProjectVO> getDetail(Long projectId) {
        BidProjectVO detail = bidProjectDao.getDetail(projectId);
        if (detail == null) {
            return ResponseDTO.error(UserErrorCode.DATA_NOT_EXIST);
        }
        fillAllowedActions(detail);
        return ResponseDTO.ok(detail);
    }

    /**
     * 查询全部项目选项
     */
    public ResponseDTO<List<BidProjectOptionVO>> queryList(String status) {
        return ResponseDTO.ok(bidProjectDao.queryList(status));
    }

    /**
     * 新增项目
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> add(BidProjectAddForm addForm) {
        ResponseDTO<String> validateResult = validateProjectForm(addForm, null);
        if (!validateResult.getOk()) {
            return validateResult;
        }

        if (existsProjectCode(addForm.getProjectCode(), null)) {
            return ResponseDTO.userErrorParam("项目编号已存在");
        }

        RequestUser requestUser = SmartRequestUtil.getRequestUser();
        BidProjectEntity entity = SmartBeanUtil.copy(addForm, BidProjectEntity.class);
        entity.setStatus(BidProjectStatusEnum.DRAFT.getCode());
        entity.setDeletedFlag(Boolean.FALSE);
        entity.setVersion(0);
        entity.setCreateUserId(requestUser == null ? null : requestUser.getUserId());
        entity.setUpdateUserId(requestUser == null ? null : requestUser.getUserId());
        bidProjectDao.insert(entity);

        bidWorkflowHistoryService.recordProjectAction(entity.getProjectId(), null, entity.getStatus(), "create-project", "新建招标项目", requestUser, entity);
        return ResponseDTO.ok();
    }

    /**
     * 编辑项目
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> update(BidProjectUpdateForm updateForm) {
        BidProjectEntity current = bidProjectDao.selectById(updateForm.getProjectId());
        if (current == null || Boolean.TRUE.equals(current.getDeletedFlag())) {
            return ResponseDTO.error(UserErrorCode.DATA_NOT_EXIST);
        }
        if (!Objects.equals(current.getVersion(), updateForm.getVersion())) {
            return ResponseDTO.userErrorParam("数据已更新，请刷新后重试");
        }
        if (!Objects.equals(current.getStatus(), BidProjectStatusEnum.DRAFT.getCode())
                && !Objects.equals(current.getStatus(), BidProjectStatusEnum.PLANNED.getCode())) {
            return ResponseDTO.userErrorParam("当前项目状态不允许编辑");
        }

        ResponseDTO<String> validateResult = validateProjectForm(updateForm, updateForm.getProjectId());
        if (!validateResult.getOk()) {
            return validateResult;
        }
        if (existsProjectCode(updateForm.getProjectCode(), updateForm.getProjectId())) {
            return ResponseDTO.userErrorParam("项目编号已存在");
        }

        BidProjectEntity updateEntity = SmartBeanUtil.copy(updateForm, BidProjectEntity.class);
        updateEntity.setUpdateUserId(SmartRequestUtil.getRequestUserId());
        Integer updateCount = bidProjectDao.updateWithVersion(updateEntity);
        if (updateCount == null || updateCount == 0) {
            return ResponseDTO.userErrorParam("数据已更新，请刷新后重试");
        }

        bidWorkflowHistoryService.recordProjectAction(current.getProjectId(), current.getStatus(), current.getStatus(), "edit-project", "编辑招标项目", SmartRequestUtil.getRequestUser(), updateEntity);
        return ResponseDTO.ok();
    }

    /**
     * 提交计划
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> submitPlan(BidProjectActionForm actionForm) {
        BidProjectEntity current = bidProjectDao.selectById(actionForm.getProjectId());
        if (current == null || Boolean.TRUE.equals(current.getDeletedFlag())) {
            return ResponseDTO.error(UserErrorCode.DATA_NOT_EXIST);
        }
        if (!Objects.equals(current.getVersion(), actionForm.getVersion())) {
            return ResponseDTO.userErrorParam("数据已更新，请刷新后重试");
        }
        if (!Objects.equals(current.getStatus(), BidProjectStatusEnum.DRAFT.getCode())) {
            return ResponseDTO.userErrorParam("只有草稿项目才能提交计划");
        }
        Long lotCount = bidProjectDao.countValidLotByProjectId(current.getProjectId());
        if (lotCount == null || lotCount <= 0) {
            return ResponseDTO.userErrorParam("请先为项目维护至少一个有效标段");
        }

        BidProjectEntity updateEntity = new BidProjectEntity();
        updateEntity.setProjectId(current.getProjectId());
        updateEntity.setVersion(actionForm.getVersion());
        updateEntity.setStatus(BidProjectStatusEnum.PLANNED.getCode());
        updateEntity.setUpdateUserId(SmartRequestUtil.getRequestUserId());
        Integer updateCount = bidProjectDao.updateWithVersion(updateEntity);
        if (updateCount == null || updateCount == 0) {
            return ResponseDTO.userErrorParam("数据已更新，请刷新后重试");
        }

        bidWorkflowHistoryService.recordProjectAction(current.getProjectId(), current.getStatus(), BidProjectStatusEnum.PLANNED.getCode(), "submit-plan", actionForm.getRemark(), SmartRequestUtil.getRequestUser(), updateEntity);
        return ResponseDTO.ok();
    }

    /**
     * 发布项目
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> publish(BidProjectActionForm actionForm) {
        BidProjectEntity current = bidProjectDao.selectById(actionForm.getProjectId());
        if (current == null || Boolean.TRUE.equals(current.getDeletedFlag())) {
            return ResponseDTO.error(UserErrorCode.DATA_NOT_EXIST);
        }
        if (!Objects.equals(current.getVersion(), actionForm.getVersion())) {
            return ResponseDTO.userErrorParam("数据已更新，请刷新后重试");
        }
        if (!Objects.equals(current.getStatus(), BidProjectStatusEnum.PLANNED.getCode())) {
            return ResponseDTO.userErrorParam("只有已提交计划的项目才能发布");
        }

        Long lotCount = bidProjectDao.countValidLotByProjectId(current.getProjectId());
        if (lotCount == null || lotCount <= 0) {
            return ResponseDTO.userErrorParam("项目下没有可发布的有效标段");
        }

        Integer updatedLots = bidLotDao.publishLotsByProjectId(current.getProjectId(), SmartRequestUtil.getRequestUserId(), BidLotStatusEnum.DRAFT.getCode(), BidLotStatusEnum.BIDDING.getCode());
        if (updatedLots == null || updatedLots <= 0) {
            return ResponseDTO.userErrorParam("项目下没有符合发布条件的标段");
        }

        BidProjectEntity updateEntity = new BidProjectEntity();
        updateEntity.setProjectId(current.getProjectId());
        updateEntity.setVersion(actionForm.getVersion());
        updateEntity.setStatus(BidProjectStatusEnum.PUBLISHED.getCode());
        updateEntity.setPublishTime(LocalDateTime.now());
        updateEntity.setUpdateUserId(SmartRequestUtil.getRequestUserId());
        Integer updateCount = bidProjectDao.updateWithVersion(updateEntity);
        if (updateCount == null || updateCount == 0) {
            return ResponseDTO.userErrorParam("数据已更新，请刷新后重试");
        }

        bidWorkflowHistoryService.recordProjectAction(current.getProjectId(), current.getStatus(), BidProjectStatusEnum.PUBLISHED.getCode(), "publish-project", actionForm.getRemark(), SmartRequestUtil.getRequestUser(), updateEntity);
        return ResponseDTO.ok();
    }

    /**
     * 作废项目
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> cancel(BidProjectActionForm actionForm) {
        BidProjectEntity current = bidProjectDao.selectById(actionForm.getProjectId());
        if (current == null || Boolean.TRUE.equals(current.getDeletedFlag())) {
            return ResponseDTO.error(UserErrorCode.DATA_NOT_EXIST);
        }
        if (!Objects.equals(current.getVersion(), actionForm.getVersion())) {
            return ResponseDTO.userErrorParam("数据已更新，请刷新后重试");
        }
        if (!Objects.equals(current.getStatus(), BidProjectStatusEnum.DRAFT.getCode())
                && !Objects.equals(current.getStatus(), BidProjectStatusEnum.PLANNED.getCode())) {
            return ResponseDTO.userErrorParam("当前项目状态不允许作废");
        }

        BidProjectEntity updateEntity = new BidProjectEntity();
        updateEntity.setProjectId(current.getProjectId());
        updateEntity.setVersion(actionForm.getVersion());
        updateEntity.setStatus(BidProjectStatusEnum.CANCELLED.getCode());
        updateEntity.setUpdateUserId(SmartRequestUtil.getRequestUserId());
        Integer updateCount = bidProjectDao.updateWithVersion(updateEntity);
        if (updateCount == null || updateCount == 0) {
            return ResponseDTO.userErrorParam("数据已更新，请刷新后重试");
        }

        bidWorkflowHistoryService.recordProjectAction(current.getProjectId(), current.getStatus(), BidProjectStatusEnum.CANCELLED.getCode(), "cancel-project", actionForm.getRemark(), SmartRequestUtil.getRequestUser(), updateEntity);
        return ResponseDTO.ok();
    }

    private ResponseDTO<String> validateProjectForm(BidProjectAddForm form, Long projectId) {
        if (!Objects.equals(form.getOwnerOrgId(), form.getAgentOrgId())) {
            DepartmentEntity agentDepartment = form.getAgentOrgId() == null ? null : departmentDao.selectById(form.getAgentOrgId());
            if (form.getAgentOrgId() != null && agentDepartment == null) {
                return ResponseDTO.userErrorParam("代理机构归属组织不存在");
            }
        }
        DepartmentEntity ownerDepartment = departmentDao.selectById(form.getOwnerOrgId());
        if (ownerDepartment == null) {
            return ResponseDTO.userErrorParam("招标人归属组织不存在");
        }
        if (form.getManagerEmployeeId() != null) {
            EmployeeEntity employeeEntity = employeeDao.selectById(form.getManagerEmployeeId());
            if (employeeEntity == null || Boolean.TRUE.equals(employeeEntity.getDeletedFlag())) {
                return ResponseDTO.userErrorParam("项目负责人不存在");
            }
        }
        if (!StringUtils.isBlank(form.getProjectType()) && !StringUtils.isBlank(form.getProcurementMode())
                && Objects.equals(form.getProjectType(), form.getProcurementMode())) {
            return ResponseDTO.userErrorParam("项目类型和采购方式不能填写为同一编码");
        }
        return ResponseDTO.ok();
    }

    private boolean existsProjectCode(String projectCode, Long excludeProjectId) {
        Long count = bidProjectDao.selectCount(Wrappers.<BidProjectEntity>lambdaQuery()
                .eq(BidProjectEntity::getProjectCode, projectCode)
                .eq(BidProjectEntity::getDeletedFlag, Boolean.FALSE)
                .ne(excludeProjectId != null, BidProjectEntity::getProjectId, excludeProjectId));
        return count != null && count > 0;
    }

    private void fillAllowedActions(BidProjectVO projectVO) {
        Long lotCount = projectVO.getLotCount() == null ? 0L : projectVO.getLotCount();
        String status = projectVO.getStatus();
        if (Objects.equals(status, BidProjectStatusEnum.DRAFT.getCode())) {
            projectVO.setAllowedActions(lotCount > 0
                    ? List.of("edit-project", "submit-plan", "cancel-project")
                    : List.of("edit-project", "cancel-project"));
            return;
        }
        if (Objects.equals(status, BidProjectStatusEnum.PLANNED.getCode())) {
            projectVO.setAllowedActions(List.of("edit-project", "publish-project", "cancel-project"));
            return;
        }
        if (Objects.equals(status, BidProjectStatusEnum.PUBLISHED.getCode())) {
            projectVO.setAllowedActions(Collections.emptyList());
            return;
        }
        projectVO.setAllowedActions(Collections.emptyList());
    }
}
