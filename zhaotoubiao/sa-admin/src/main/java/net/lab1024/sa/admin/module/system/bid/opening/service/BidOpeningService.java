package net.lab1024.sa.admin.module.system.bid.opening.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import net.lab1024.sa.admin.module.system.bid.common.constant.BidLotStatusEnum;
import net.lab1024.sa.admin.module.system.bid.common.constant.BidOpeningStatusEnum;
import net.lab1024.sa.admin.module.system.bid.common.constant.BidProjectStatusEnum;
import net.lab1024.sa.admin.module.system.bid.common.service.BidWorkflowHistoryService;
import net.lab1024.sa.admin.module.system.bid.lot.dao.BidLotDao;
import net.lab1024.sa.admin.module.system.bid.lot.domain.entity.BidLotEntity;
import net.lab1024.sa.admin.module.system.bid.opening.dao.BidOpeningDao;
import net.lab1024.sa.admin.module.system.bid.opening.domain.entity.BidOpeningEntity;
import net.lab1024.sa.admin.module.system.bid.opening.domain.form.BidOpeningActionForm;
import net.lab1024.sa.admin.module.system.bid.opening.domain.form.BidOpeningCreateForm;
import net.lab1024.sa.admin.module.system.bid.opening.domain.form.BidOpeningQueryForm;
import net.lab1024.sa.admin.module.system.bid.opening.domain.vo.BidOpeningVO;
import net.lab1024.sa.admin.module.system.bid.project.dao.BidProjectDao;
import net.lab1024.sa.admin.module.system.bid.project.domain.entity.BidProjectEntity;
import net.lab1024.sa.base.common.code.UserErrorCode;
import net.lab1024.sa.base.common.domain.PageResult;
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
 * 开标 Service
 *
 * @author Codex
 * @date 2026-06-08
 */
@Service
public class BidOpeningService {

    @Resource
    private BidOpeningDao bidOpeningDao;

    @Resource
    private BidProjectDao bidProjectDao;

    @Resource
    private BidLotDao bidLotDao;

    @Resource
    private BidWorkflowHistoryService bidWorkflowHistoryService;

    /**
     * 分页查询
     */
    public PageResult<BidOpeningVO> queryPage(BidOpeningQueryForm queryForm) {
        queryForm.setDeletedFlag(Boolean.FALSE);
        Page<?> page = SmartPageUtil.convert2PageQuery(queryForm);
        List<BidOpeningVO> list = bidOpeningDao.queryPage(page, queryForm);
        if (list != null) {
            list.forEach(this::fillAllowedActions);
        }
        return SmartPageUtil.convert2PageResult(page, list);
    }

    /**
     * 查询详情
     */
    public ResponseDTO<BidOpeningVO> getDetail(Long openingId) {
        BidOpeningVO detail = bidOpeningDao.getDetail(openingId);
        if (detail == null) {
            return ResponseDTO.error(UserErrorCode.DATA_NOT_EXIST);
        }
        fillDetail(detail);
        return ResponseDTO.ok(detail);
    }

    /**
     * 查询标段开标记录
     */
    public ResponseDTO<BidOpeningVO> getByLotId(Long lotId) {
        BidOpeningVO detail = bidOpeningDao.getByLotId(lotId);
        if (detail == null) {
            return ResponseDTO.error(UserErrorCode.DATA_NOT_EXIST);
        }
        fillDetail(detail);
        return ResponseDTO.ok(detail);
    }

    /**
     * 创建开标记录
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> create(BidOpeningCreateForm createForm) {
        ResponseDTO<String> validateResult = validateCreateForm(createForm);
        if (!validateResult.getOk()) {
            return validateResult;
        }

        BidOpeningEntity entity = SmartBeanUtil.copy(createForm, BidOpeningEntity.class);
        entity.setStatus(BidOpeningStatusEnum.PENDING.getCode());
        entity.setAbnormalFlag(Boolean.FALSE);
        entity.setDeletedFlag(Boolean.FALSE);
        entity.setVersion(0);
        entity.setCreateUserId(SmartRequestUtil.getRequestUserId());
        entity.setUpdateUserId(SmartRequestUtil.getRequestUserId());
        bidOpeningDao.insert(entity);

        bidWorkflowHistoryService.recordOpeningAction(entity.getOpeningId(), entity.getProjectId(), entity.getLotId(),
                null, entity.getStatus(), "create-opening", "创建开标记录", SmartRequestUtil.getRequestUser(), entity);
        return ResponseDTO.ok();
    }

    /**
     * 开始开标
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> start(BidOpeningActionForm actionForm) {
        BidOpeningEntity current = getCurrent(actionForm.getOpeningId());
        if (current == null) {
            return ResponseDTO.error(UserErrorCode.DATA_NOT_EXIST);
        }
        if (!Objects.equals(current.getVersion(), actionForm.getVersion())) {
            return ResponseDTO.userErrorParam("数据已更新，请刷新后重试");
        }
        if (!Objects.equals(current.getStatus(), BidOpeningStatusEnum.PENDING.getCode())) {
            return ResponseDTO.userErrorParam("只有待开标记录才能开始开标");
        }

        BidOpeningEntity updateEntity = new BidOpeningEntity();
        updateEntity.setOpeningId(current.getOpeningId());
        updateEntity.setVersion(actionForm.getVersion());
        updateEntity.setStatus(BidOpeningStatusEnum.IN_PROGRESS.getCode());
        updateEntity.setOpeningTime(current.getOpeningTime() == null ? LocalDateTime.now() : current.getOpeningTime());
        updateEntity.setUpdateUserId(SmartRequestUtil.getRequestUserId());
        Integer updateCount = bidOpeningDao.updateWithVersion(updateEntity);
        if (updateCount == null || updateCount == 0) {
            return ResponseDTO.userErrorParam("数据已更新，请刷新后重试");
        }
        updateProjectStatus(current.getProjectId(), BidProjectStatusEnum.OPENING_IN_PROGRESS.getCode());

        bidWorkflowHistoryService.recordOpeningAction(current.getOpeningId(), current.getProjectId(), current.getLotId(),
                current.getStatus(), BidOpeningStatusEnum.IN_PROGRESS.getCode(), "start-opening", actionForm.getRemark(), SmartRequestUtil.getRequestUser(), updateEntity);
        return ResponseDTO.ok();
    }

    /**
     * 完成开标
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> complete(BidOpeningActionForm actionForm) {
        BidOpeningEntity current = getCurrent(actionForm.getOpeningId());
        if (current == null) {
            return ResponseDTO.error(UserErrorCode.DATA_NOT_EXIST);
        }
        if (!Objects.equals(current.getVersion(), actionForm.getVersion())) {
            return ResponseDTO.userErrorParam("数据已更新，请刷新后重试");
        }
        if (!Objects.equals(current.getStatus(), BidOpeningStatusEnum.IN_PROGRESS.getCode())) {
            return ResponseDTO.userErrorParam("只有开标中记录才能完成开标");
        }

        Long itemCount = bidOpeningDao.countItems(current.getOpeningId());
        if (itemCount == null || itemCount == 0) {
            bidOpeningDao.insertItemsFromSubmissions(current.getOpeningId(), current.getLotId());
            itemCount = bidOpeningDao.countItems(current.getOpeningId());
        }
        if (itemCount == null || itemCount == 0) {
            return ResponseDTO.userErrorParam("当前标段没有可开标的有效投标");
        }

        BidOpeningEntity updateEntity = new BidOpeningEntity();
        updateEntity.setOpeningId(current.getOpeningId());
        updateEntity.setVersion(actionForm.getVersion());
        updateEntity.setStatus(BidOpeningStatusEnum.COMPLETED.getCode());
        updateEntity.setSummary(StringUtils.defaultIfBlank(actionForm.getRemark(), current.getSummary()));
        updateEntity.setUpdateUserId(SmartRequestUtil.getRequestUserId());
        Integer updateCount = bidOpeningDao.updateWithVersion(updateEntity);
        if (updateCount == null || updateCount == 0) {
            return ResponseDTO.userErrorParam("数据已更新，请刷新后重试");
        }

        updateLotStatus(current.getLotId(), BidLotStatusEnum.OPENED.getCode());
        bidOpeningDao.markSubmissionsOpenedByOpening(current.getOpeningId(), SmartRequestUtil.getRequestUserId());

        bidWorkflowHistoryService.recordOpeningAction(current.getOpeningId(), current.getProjectId(), current.getLotId(),
                current.getStatus(), BidOpeningStatusEnum.COMPLETED.getCode(), "complete-opening", actionForm.getRemark(), SmartRequestUtil.getRequestUser(), updateEntity);
        return ResponseDTO.ok();
    }

    /**
     * 异常关闭开标
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> abnormalClose(BidOpeningActionForm actionForm) {
        BidOpeningEntity current = getCurrent(actionForm.getOpeningId());
        if (current == null) {
            return ResponseDTO.error(UserErrorCode.DATA_NOT_EXIST);
        }
        if (!Objects.equals(current.getVersion(), actionForm.getVersion())) {
            return ResponseDTO.userErrorParam("数据已更新，请刷新后重试");
        }
        if (!Objects.equals(current.getStatus(), BidOpeningStatusEnum.PENDING.getCode())
                && !Objects.equals(current.getStatus(), BidOpeningStatusEnum.IN_PROGRESS.getCode())) {
            return ResponseDTO.userErrorParam("当前开标状态不允许异常关闭");
        }
        if (StringUtils.isBlank(actionForm.getRemark())) {
            return ResponseDTO.userErrorParam("异常关闭原因不能为空");
        }

        BidOpeningEntity updateEntity = new BidOpeningEntity();
        updateEntity.setOpeningId(current.getOpeningId());
        updateEntity.setVersion(actionForm.getVersion());
        updateEntity.setStatus(BidOpeningStatusEnum.ABNORMAL_CLOSED.getCode());
        updateEntity.setAbnormalFlag(Boolean.TRUE);
        updateEntity.setAbnormalReason(actionForm.getRemark());
        updateEntity.setUpdateUserId(SmartRequestUtil.getRequestUserId());
        Integer updateCount = bidOpeningDao.updateWithVersion(updateEntity);
        if (updateCount == null || updateCount == 0) {
            return ResponseDTO.userErrorParam("数据已更新，请刷新后重试");
        }

        bidWorkflowHistoryService.recordOpeningAction(current.getOpeningId(), current.getProjectId(), current.getLotId(),
                current.getStatus(), BidOpeningStatusEnum.ABNORMAL_CLOSED.getCode(), "abnormal-close-opening", actionForm.getRemark(), SmartRequestUtil.getRequestUser(), updateEntity);
        return ResponseDTO.ok();
    }

    private ResponseDTO<String> validateCreateForm(BidOpeningCreateForm form) {
        BidProjectEntity project = bidProjectDao.selectById(form.getProjectId());
        if (project == null || Boolean.TRUE.equals(project.getDeletedFlag())) {
            return ResponseDTO.userErrorParam("所属项目不存在");
        }

        BidLotEntity lot = bidLotDao.selectById(form.getLotId());
        if (lot == null || Boolean.TRUE.equals(lot.getDeletedFlag())) {
            return ResponseDTO.userErrorParam("所属标段不存在");
        }
        if (!Objects.equals(lot.getProjectId(), form.getProjectId())) {
            return ResponseDTO.userErrorParam("标段不属于当前项目");
        }
        if (!Objects.equals(lot.getStatus(), BidLotStatusEnum.BID_CLOSED.getCode())) {
            return ResponseDTO.userErrorParam("只有已截标标段才能创建开标记录");
        }

        Long count = bidOpeningDao.selectCount(Wrappers.<BidOpeningEntity>lambdaQuery()
                .eq(BidOpeningEntity::getLotId, form.getLotId())
                .eq(BidOpeningEntity::getDeletedFlag, Boolean.FALSE));
        if (count != null && count > 0) {
            return ResponseDTO.userErrorParam("当前标段已存在开标记录");
        }
        return ResponseDTO.ok();
    }

    private BidOpeningEntity getCurrent(Long openingId) {
        BidOpeningEntity current = bidOpeningDao.selectById(openingId);
        if (current == null || Boolean.TRUE.equals(current.getDeletedFlag())) {
            return null;
        }
        return current;
    }

    private void updateLotStatus(Long lotId, String status) {
        BidLotEntity lot = bidLotDao.selectById(lotId);
        if (lot == null || Boolean.TRUE.equals(lot.getDeletedFlag()) || Objects.equals(lot.getStatus(), status)) {
            return;
        }
        BidLotEntity updateEntity = new BidLotEntity();
        updateEntity.setLotId(lotId);
        updateEntity.setVersion(lot.getVersion());
        updateEntity.setStatus(status);
        updateEntity.setUpdateUserId(SmartRequestUtil.getRequestUserId());
        bidLotDao.updateWithVersion(updateEntity);
    }

    private void updateProjectStatus(Long projectId, String status) {
        BidProjectEntity project = bidProjectDao.selectById(projectId);
        if (project == null || Boolean.TRUE.equals(project.getDeletedFlag()) || Objects.equals(project.getStatus(), status)) {
            return;
        }
        BidProjectEntity updateEntity = new BidProjectEntity();
        updateEntity.setProjectId(projectId);
        updateEntity.setVersion(project.getVersion());
        updateEntity.setStatus(status);
        updateEntity.setUpdateUserId(SmartRequestUtil.getRequestUserId());
        bidProjectDao.updateWithVersion(updateEntity);
    }

    private void fillAllowedActions(BidOpeningVO openingVO) {
        if (Objects.equals(openingVO.getStatus(), BidOpeningStatusEnum.PENDING.getCode())) {
            openingVO.setAllowedActions(List.of("start-opening", "abnormal-close-opening"));
            return;
        }
        if (Objects.equals(openingVO.getStatus(), BidOpeningStatusEnum.IN_PROGRESS.getCode())) {
            openingVO.setAllowedActions(List.of("complete-opening", "abnormal-close-opening"));
            return;
        }
        openingVO.setAllowedActions(Collections.emptyList());
    }

    private void fillDetail(BidOpeningVO openingVO) {
        fillAllowedActions(openingVO);
        openingVO.setItemList(bidOpeningDao.listItems(openingVO.getOpeningId()));
    }
}
