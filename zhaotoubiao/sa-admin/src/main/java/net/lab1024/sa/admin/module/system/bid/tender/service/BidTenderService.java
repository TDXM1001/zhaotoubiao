package net.lab1024.sa.admin.module.system.bid.tender.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import net.lab1024.sa.admin.module.system.bid.attachment.service.BidAttachmentService;
import net.lab1024.sa.admin.module.system.bid.common.constant.BidBusinessTypeEnum;
import net.lab1024.sa.admin.module.system.bid.common.constant.BidLotStatusEnum;
import net.lab1024.sa.admin.module.system.bid.common.constant.BidTenderStatusEnum;
import net.lab1024.sa.admin.module.system.bid.common.constant.BidTenderVersionTypeEnum;
import net.lab1024.sa.admin.module.system.bid.common.service.BidWorkflowHistoryService;
import net.lab1024.sa.admin.module.system.bid.lot.dao.BidLotDao;
import net.lab1024.sa.admin.module.system.bid.lot.domain.entity.BidLotEntity;
import net.lab1024.sa.admin.module.system.bid.project.dao.BidProjectDao;
import net.lab1024.sa.admin.module.system.bid.project.domain.entity.BidProjectEntity;
import net.lab1024.sa.admin.module.system.bid.tender.dao.BidTenderDao;
import net.lab1024.sa.admin.module.system.bid.tender.domain.entity.BidTenderVersionEntity;
import net.lab1024.sa.admin.module.system.bid.tender.domain.form.BidTenderActionForm;
import net.lab1024.sa.admin.module.system.bid.tender.domain.form.BidTenderCreateForm;
import net.lab1024.sa.admin.module.system.bid.tender.domain.form.BidTenderQueryForm;
import net.lab1024.sa.admin.module.system.bid.tender.domain.form.BidTenderUpdateForm;
import net.lab1024.sa.admin.module.system.bid.tender.domain.vo.BidTenderVO;
import net.lab1024.sa.base.common.code.UserErrorCode;
import net.lab1024.sa.base.common.domain.PageResult;
import net.lab1024.sa.base.common.domain.ResponseDTO;
import net.lab1024.sa.base.common.util.SmartBeanUtil;
import net.lab1024.sa.base.common.util.SmartPageUtil;
import net.lab1024.sa.base.common.util.SmartRequestUtil;
import net.lab1024.sa.base.module.support.file.domain.vo.FileDownloadVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 招标文件 Service
 *
 * @author Codex
 * @date 2026-06-08
 */
@Service
public class BidTenderService {

    @Resource
    private BidTenderDao bidTenderDao;

    @Resource
    private BidProjectDao bidProjectDao;

    @Resource
    private BidLotDao bidLotDao;

    @Resource
    private BidWorkflowHistoryService bidWorkflowHistoryService;

    @Resource
    private BidAttachmentService bidAttachmentService;

    /**
     * 分页查询
     */
    public PageResult<BidTenderVO> queryPage(BidTenderQueryForm queryForm) {
        queryForm.setDeletedFlag(Boolean.FALSE);
        Page<?> page = SmartPageUtil.convert2PageQuery(queryForm);
        List<BidTenderVO> list = bidTenderDao.queryPage(page, queryForm);
        if (list != null) {
            list.forEach(this::fillAllowedActions);
        }
        return SmartPageUtil.convert2PageResult(page, list);
    }

    /**
     * 查询详情
     */
    public ResponseDTO<BidTenderVO> getDetail(Long tenderVersionId) {
        BidTenderVO detail = bidTenderDao.getDetail(tenderVersionId);
        if (detail == null) {
            return ResponseDTO.error(UserErrorCode.DATA_NOT_EXIST);
        }
        fillDetail(detail);
        return ResponseDTO.ok(detail);
    }

    /**
     * 查询标段当前有效版本
     */
    public ResponseDTO<BidTenderVO> getActiveByLotId(Long lotId) {
        BidTenderVO detail = bidTenderDao.getActiveByLotId(lotId);
        if (detail == null) {
            return ResponseDTO.error(UserErrorCode.DATA_NOT_EXIST);
        }
        fillDetail(detail);
        return ResponseDTO.ok(detail);
    }

    /**
     * 下载招标文件附件
     */
    public ResponseDTO<FileDownloadVO> downloadAttachment(Long tenderVersionId, Long attachmentId, String userAgent) {
        BidTenderVersionEntity current = getCurrent(tenderVersionId);
        if (current == null) {
            return ResponseDTO.error(UserErrorCode.DATA_NOT_EXIST);
        }
        return bidAttachmentService.download(BidBusinessTypeEnum.TENDER_VERSION.getCode(), tenderVersionId, attachmentId, userAgent);
    }

    /**
     * 查询招标文件附件预览地址
     */
    public ResponseDTO<String> getAttachmentPreviewUrl(Long tenderVersionId, Long attachmentId) {
        BidTenderVersionEntity current = getCurrent(tenderVersionId);
        if (current == null) {
            return ResponseDTO.error(UserErrorCode.DATA_NOT_EXIST);
        }
        return bidAttachmentService.getPreviewUrl(BidBusinessTypeEnum.TENDER_VERSION.getCode(), tenderVersionId, attachmentId);
    }

    /**
     * 新增招标文件版本草稿
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> create(BidTenderCreateForm createForm) {
        ResponseDTO<String> validateResult = validateTenderForm(createForm);
        if (!validateResult.getOk()) {
            return validateResult;
        }

        BidTenderVersionEntity entity = SmartBeanUtil.copy(createForm, BidTenderVersionEntity.class);
        entity.setVersionNo(nextVersionNo(createForm.getLotId()));
        entity.setStatus(BidTenderStatusEnum.DRAFT.getCode());
        entity.setCurrentFlag(null);
        entity.setDeletedFlag(Boolean.FALSE);
        entity.setVersion(0);
        entity.setCreateUserId(SmartRequestUtil.getRequestUserId());
        entity.setUpdateUserId(SmartRequestUtil.getRequestUserId());
        bidTenderDao.insert(entity);

        bidWorkflowHistoryService.recordTenderAction(entity.getTenderVersionId(), entity.getProjectId(), entity.getLotId(),
                null, entity.getStatus(), "create-tender", "新增招标文件版本", SmartRequestUtil.getRequestUser(), entity);
        return ResponseDTO.ok();
    }

    /**
     * 编辑招标文件版本草稿
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> update(BidTenderUpdateForm updateForm) {
        BidTenderVersionEntity current = getCurrent(updateForm.getTenderVersionId());
        if (current == null) {
            return ResponseDTO.error(UserErrorCode.DATA_NOT_EXIST);
        }
        if (!Objects.equals(current.getVersion(), updateForm.getVersion())) {
            return ResponseDTO.userErrorParam("数据已更新，请刷新后重试");
        }
        if (!Objects.equals(current.getStatus(), BidTenderStatusEnum.DRAFT.getCode())) {
            return ResponseDTO.userErrorParam("只有草稿招标文件才能编辑");
        }

        ResponseDTO<String> validateResult = validateTenderForm(updateForm);
        if (!validateResult.getOk()) {
            return validateResult;
        }
        if (!Objects.equals(current.getProjectId(), updateForm.getProjectId())
                || !Objects.equals(current.getLotId(), updateForm.getLotId())) {
            return ResponseDTO.userErrorParam("草稿招标文件不允许更换所属项目或标段");
        }

        BidTenderVersionEntity updateEntity = SmartBeanUtil.copy(updateForm, BidTenderVersionEntity.class);
        updateEntity.setUpdateUserId(SmartRequestUtil.getRequestUserId());
        Integer updateCount = bidTenderDao.updateWithVersion(updateEntity);
        if (updateCount == null || updateCount == 0) {
            return ResponseDTO.userErrorParam("数据已更新，请刷新后重试");
        }

        bidWorkflowHistoryService.recordTenderAction(current.getTenderVersionId(), current.getProjectId(), current.getLotId(),
                current.getStatus(), current.getStatus(), "edit-tender", "编辑招标文件版本", SmartRequestUtil.getRequestUser(), updateEntity);
        return ResponseDTO.ok();
    }

    /**
     * 发布招标文件版本
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> publish(BidTenderActionForm actionForm) {
        BidTenderVersionEntity current = validateActionCurrent(actionForm);
        if (current == null) {
            return ResponseDTO.error(UserErrorCode.DATA_NOT_EXIST);
        }
        if (!Objects.equals(current.getVersion(), actionForm.getVersion())) {
            return ResponseDTO.userErrorParam("数据已更新，请刷新后重试");
        }
        if (!Objects.equals(current.getStatus(), BidTenderStatusEnum.DRAFT.getCode())) {
            return ResponseDTO.userErrorParam("只有草稿招标文件才能发布");
        }

        bidTenderDao.supersedeCurrentByLotId(current.getLotId(), current.getTenderVersionId(), SmartRequestUtil.getRequestUserId());

        BidTenderVersionEntity updateEntity = new BidTenderVersionEntity();
        updateEntity.setTenderVersionId(current.getTenderVersionId());
        updateEntity.setVersion(actionForm.getVersion());
        updateEntity.setStatus(BidTenderStatusEnum.ACTIVE.getCode());
        updateEntity.setCurrentFlag(Boolean.TRUE);
        updateEntity.setEffectiveTime(LocalDateTime.now());
        updateEntity.setPublishTime(LocalDateTime.now());
        updateEntity.setPublishUserId(SmartRequestUtil.getRequestUserId());
        updateEntity.setUpdateUserId(SmartRequestUtil.getRequestUserId());
        Integer updateCount = bidTenderDao.updateWithVersion(updateEntity);
        if (updateCount == null || updateCount == 0) {
            return ResponseDTO.userErrorParam("数据已更新，请刷新后重试");
        }

        bidWorkflowHistoryService.recordTenderAction(current.getTenderVersionId(), current.getProjectId(), current.getLotId(),
                current.getStatus(), BidTenderStatusEnum.ACTIVE.getCode(), "publish-tender", actionForm.getRemark(), SmartRequestUtil.getRequestUser(), updateEntity);
        return ResponseDTO.ok();
    }

    /**
     * 发布澄清版本
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> clarify(BidTenderActionForm actionForm) {
        BidTenderVersionEntity current = validateActionCurrent(actionForm);
        if (current == null) {
            return ResponseDTO.error(UserErrorCode.DATA_NOT_EXIST);
        }
        if (!Objects.equals(current.getVersion(), actionForm.getVersion())) {
            return ResponseDTO.userErrorParam("数据已更新，请刷新后重试");
        }
        if (!Objects.equals(current.getStatus(), BidTenderStatusEnum.ACTIVE.getCode())
                || !Boolean.TRUE.equals(current.getCurrentFlag())) {
            return ResponseDTO.userErrorParam("只有当前有效招标文件才能发布澄清");
        }

        BidTenderVersionEntity oldActive = new BidTenderVersionEntity();
        oldActive.setTenderVersionId(current.getTenderVersionId());
        oldActive.setVersion(actionForm.getVersion());
        oldActive.setStatus(BidTenderStatusEnum.SUPERSEDED.getCode());
        oldActive.setCurrentFlag(null);
        oldActive.setUpdateUserId(SmartRequestUtil.getRequestUserId());
        Integer oldUpdateCount = bidTenderDao.updateWithVersion(oldActive);
        if (oldUpdateCount == null || oldUpdateCount == 0) {
            return ResponseDTO.userErrorParam("数据已更新，请刷新后重试");
        }

        BidTenderVersionEntity clarifyEntity = new BidTenderVersionEntity();
        clarifyEntity.setProjectId(current.getProjectId());
        clarifyEntity.setLotId(current.getLotId());
        clarifyEntity.setVersionNo(nextVersionNo(current.getLotId()));
        clarifyEntity.setVersionType(BidTenderVersionTypeEnum.CLARIFICATION.getCode());
        clarifyEntity.setStatus(BidTenderStatusEnum.ACTIVE.getCode());
        clarifyEntity.setCurrentFlag(Boolean.TRUE);
        clarifyEntity.setParentVersionId(current.getTenderVersionId());
        clarifyEntity.setEffectiveTime(LocalDateTime.now());
        clarifyEntity.setPublishUserId(SmartRequestUtil.getRequestUserId());
        clarifyEntity.setPublishTime(LocalDateTime.now());
        clarifyEntity.setSummary(StringUtils.trimToEmpty(actionForm.getRemark()));
        clarifyEntity.setRemark(actionForm.getRemark());
        clarifyEntity.setDeletedFlag(Boolean.FALSE);
        clarifyEntity.setVersion(0);
        clarifyEntity.setCreateUserId(SmartRequestUtil.getRequestUserId());
        clarifyEntity.setUpdateUserId(SmartRequestUtil.getRequestUserId());
        bidTenderDao.insert(clarifyEntity);

        bidWorkflowHistoryService.recordTenderAction(current.getTenderVersionId(), current.getProjectId(), current.getLotId(),
                current.getStatus(), BidTenderStatusEnum.SUPERSEDED.getCode(), "clarify-tender", actionForm.getRemark(), SmartRequestUtil.getRequestUser(), clarifyEntity);
        return ResponseDTO.ok();
    }

    /**
     * 撤回招标文件版本
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> withdraw(BidTenderActionForm actionForm) {
        BidTenderVersionEntity current = validateActionCurrent(actionForm);
        if (current == null) {
            return ResponseDTO.error(UserErrorCode.DATA_NOT_EXIST);
        }
        if (!Objects.equals(current.getVersion(), actionForm.getVersion())) {
            return ResponseDTO.userErrorParam("数据已更新，请刷新后重试");
        }
        if (!Objects.equals(current.getStatus(), BidTenderStatusEnum.DRAFT.getCode())
                && !Objects.equals(current.getStatus(), BidTenderStatusEnum.ACTIVE.getCode())) {
            return ResponseDTO.userErrorParam("当前招标文件状态不允许撤回");
        }

        BidTenderVersionEntity updateEntity = new BidTenderVersionEntity();
        updateEntity.setTenderVersionId(current.getTenderVersionId());
        updateEntity.setVersion(actionForm.getVersion());
        updateEntity.setStatus(BidTenderStatusEnum.WITHDRAWN.getCode());
        updateEntity.setCurrentFlag(null);
        updateEntity.setUpdateUserId(SmartRequestUtil.getRequestUserId());
        Integer updateCount = bidTenderDao.updateWithVersion(updateEntity);
        if (updateCount == null || updateCount == 0) {
            return ResponseDTO.userErrorParam("数据已更新，请刷新后重试");
        }

        bidWorkflowHistoryService.recordTenderAction(current.getTenderVersionId(), current.getProjectId(), current.getLotId(),
                current.getStatus(), BidTenderStatusEnum.WITHDRAWN.getCode(), "withdraw-tender", actionForm.getRemark(), SmartRequestUtil.getRequestUser(), updateEntity);
        return ResponseDTO.ok();
    }

    private ResponseDTO<String> validateTenderForm(BidTenderCreateForm form) {
        if (!BidTenderVersionTypeEnum.contains(form.getVersionType())) {
            return ResponseDTO.userErrorParam("招标文件版本类型不正确");
        }

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
        if (Objects.equals(lot.getStatus(), BidLotStatusEnum.VOIDED.getCode())) {
            return ResponseDTO.userErrorParam("已废止标段不允许维护招标文件");
        }
        return ResponseDTO.ok();
    }

    private BidTenderVersionEntity validateActionCurrent(BidTenderActionForm actionForm) {
        BidTenderVersionEntity current = getCurrent(actionForm.getTenderVersionId());
        if (current == null) {
            return null;
        }
        return current;
    }

    private BidTenderVersionEntity getCurrent(Long tenderVersionId) {
        BidTenderVersionEntity current = bidTenderDao.selectById(tenderVersionId);
        if (current == null || Boolean.TRUE.equals(current.getDeletedFlag())) {
            return null;
        }
        return current;
    }

    private Integer nextVersionNo(Long lotId) {
        Integer maxVersionNo = bidTenderDao.selectMaxVersionNoByLotId(lotId);
        return maxVersionNo == null ? 1 : maxVersionNo + 1;
    }

    private void fillAllowedActions(BidTenderVO tenderVO) {
        if (Objects.equals(tenderVO.getStatus(), BidTenderStatusEnum.DRAFT.getCode())) {
            tenderVO.setAllowedActions(List.of("edit-tender", "publish-tender", "withdraw-tender"));
            return;
        }
        if (Objects.equals(tenderVO.getStatus(), BidTenderStatusEnum.ACTIVE.getCode())) {
            tenderVO.setAllowedActions(List.of("clarify-tender", "withdraw-tender"));
            return;
        }
        tenderVO.setAllowedActions(Collections.emptyList());
    }

    private void fillDetail(BidTenderVO tenderVO) {
        fillAllowedActions(tenderVO);
        tenderVO.setAttachments(bidAttachmentService.listByBusiness(
                BidBusinessTypeEnum.TENDER_VERSION.getCode(),
                tenderVO.getTenderVersionId(),
                "/bid/tenders/" + tenderVO.getTenderVersionId()));
    }
}
