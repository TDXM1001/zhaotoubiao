package net.lab1024.sa.admin.module.system.bid.submission.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import net.lab1024.sa.admin.module.system.bid.attachment.service.BidAttachmentService;
import net.lab1024.sa.admin.module.system.bid.common.constant.BidBusinessTypeEnum;
import net.lab1024.sa.admin.module.system.bid.common.constant.BidLotStatusEnum;
import net.lab1024.sa.admin.module.system.bid.common.constant.BidRegistrationStatusEnum;
import net.lab1024.sa.admin.module.system.bid.common.constant.BidSubmissionStatusEnum;
import net.lab1024.sa.admin.module.system.bid.common.service.BidWorkflowHistoryService;
import net.lab1024.sa.admin.module.system.bid.lot.dao.BidLotDao;
import net.lab1024.sa.admin.module.system.bid.lot.domain.entity.BidLotEntity;
import net.lab1024.sa.admin.module.system.bid.project.dao.BidProjectDao;
import net.lab1024.sa.admin.module.system.bid.project.domain.entity.BidProjectEntity;
import net.lab1024.sa.admin.module.system.bid.registration.dao.BidRegistrationDao;
import net.lab1024.sa.admin.module.system.bid.registration.domain.entity.BidRegistrationEntity;
import net.lab1024.sa.admin.module.system.bid.submission.dao.BidSubmissionDao;
import net.lab1024.sa.admin.module.system.bid.submission.domain.entity.BidSubmissionEntity;
import net.lab1024.sa.admin.module.system.bid.submission.domain.entity.BidSubmissionVersionEntity;
import net.lab1024.sa.admin.module.system.bid.submission.domain.form.BidSubmissionActionForm;
import net.lab1024.sa.admin.module.system.bid.submission.domain.form.BidSubmissionCreateForm;
import net.lab1024.sa.admin.module.system.bid.submission.domain.form.BidSubmissionQueryForm;
import net.lab1024.sa.admin.module.system.bid.submission.domain.form.BidSubmissionSubmitForm;
import net.lab1024.sa.admin.module.system.bid.submission.domain.vo.BidSubmissionVO;
import net.lab1024.sa.base.common.code.UserErrorCode;
import net.lab1024.sa.base.common.domain.PageResult;
import net.lab1024.sa.base.common.domain.ResponseDTO;
import net.lab1024.sa.base.common.util.SmartPageUtil;
import net.lab1024.sa.base.common.util.SmartRequestUtil;
import net.lab1024.sa.base.module.support.file.domain.vo.FileDownloadVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 投标提交 Service
 *
 * @author Codex
 * @date 2026-06-08
 */
@Service
public class BidSubmissionService {

    private static final DateTimeFormatter RECEIPT_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    @Resource
    private BidSubmissionDao bidSubmissionDao;

    @Resource
    private BidProjectDao bidProjectDao;

    @Resource
    private BidLotDao bidLotDao;

    @Resource
    private BidRegistrationDao bidRegistrationDao;

    @Resource
    private BidWorkflowHistoryService bidWorkflowHistoryService;

    @Resource
    private BidAttachmentService bidAttachmentService;

    /**
     * 分页查询
     */
    public PageResult<BidSubmissionVO> queryPage(BidSubmissionQueryForm queryForm) {
        queryForm.setDeletedFlag(Boolean.FALSE);
        Page<?> page = SmartPageUtil.convert2PageQuery(queryForm);
        List<BidSubmissionVO> list = bidSubmissionDao.queryPage(page, queryForm);
        if (list != null) {
            list.forEach(this::fillAllowedActions);
        }
        return SmartPageUtil.convert2PageResult(page, list);
    }

    /**
     * 查询详情
     */
    public ResponseDTO<BidSubmissionVO> getDetail(Long submissionId) {
        BidSubmissionVO detail = bidSubmissionDao.getDetail(submissionId);
        if (detail == null) {
            return ResponseDTO.error(UserErrorCode.DATA_NOT_EXIST);
        }
        fillDetail(detail);
        return ResponseDTO.ok(detail);
    }

    /**
     * 按报名查询投标主记录
     */
    public ResponseDTO<BidSubmissionVO> getByRegistrationId(Long registrationId) {
        BidSubmissionVO detail = bidSubmissionDao.getByRegistrationId(registrationId);
        if (detail == null) {
            return ResponseDTO.error(UserErrorCode.DATA_NOT_EXIST);
        }
        fillDetail(detail);
        return ResponseDTO.ok(detail);
    }

    /**
     * 下载最新投标版本附件
     */
    public ResponseDTO<FileDownloadVO> downloadAttachment(Long submissionId, Long attachmentId, String userAgent) {
        BidSubmissionVO detail = bidSubmissionDao.getDetail(submissionId);
        if (detail == null || detail.getLatestSubmissionVersionId() == null) {
            return ResponseDTO.error(UserErrorCode.DATA_NOT_EXIST);
        }
        return bidAttachmentService.download(BidBusinessTypeEnum.SUBMISSION_VERSION.getCode(),
                detail.getLatestSubmissionVersionId(), attachmentId, userAgent);
    }

    /**
     * 查询最新投标版本附件预览地址
     */
    public ResponseDTO<String> getAttachmentPreviewUrl(Long submissionId, Long attachmentId) {
        BidSubmissionVO detail = bidSubmissionDao.getDetail(submissionId);
        if (detail == null || detail.getLatestSubmissionVersionId() == null) {
            return ResponseDTO.error(UserErrorCode.DATA_NOT_EXIST);
        }
        return bidAttachmentService.getPreviewUrl(BidBusinessTypeEnum.SUBMISSION_VERSION.getCode(),
                detail.getLatestSubmissionVersionId(), attachmentId);
    }

    /**
     * 新增投标主记录
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> create(BidSubmissionCreateForm createForm) {
        BidRegistrationEntity registration = bidRegistrationDao.selectById(createForm.getRegistrationId());
        ResponseDTO<String> validateResult = validateCreateForm(createForm, registration);
        if (!validateResult.getOk()) {
            return validateResult;
        }
        if (existsActiveSubmission(registration)) {
            return ResponseDTO.userErrorParam("该供应商已存在当前标段投标记录，请勿重复创建");
        }

        BidSubmissionEntity entity = new BidSubmissionEntity();
        entity.setProjectId(registration.getProjectId());
        entity.setLotId(registration.getLotId());
        entity.setRegistrationId(registration.getRegistrationId());
        entity.setSupplierEnterpriseId(registration.getSupplierEnterpriseId());
        entity.setSupplierNameSnapshot(registration.getSupplierNameSnapshot());
        entity.setSupplierCreditCode(registration.getSupplierCreditCode());
        entity.setStatus(BidSubmissionStatusEnum.QUALIFIED.getCode());
        entity.setLatestVersionNo(0);
        entity.setRemark(createForm.getRemark());
        entity.setDeletedFlag(Boolean.FALSE);
        entity.setVersion(0);
        entity.setCreateUserId(SmartRequestUtil.getRequestUserId());
        entity.setUpdateUserId(SmartRequestUtil.getRequestUserId());
        bidSubmissionDao.insert(entity);

        bidWorkflowHistoryService.recordSubmissionAction(entity.getSubmissionId(), entity.getProjectId(), entity.getLotId(),
                null, entity.getStatus(), "create-submission", "新增投标主记录", SmartRequestUtil.getRequestUser(), entity);
        return ResponseDTO.ok();
    }

    /**
     * 提交投标
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> submitBid(BidSubmissionSubmitForm submitForm) {
        BidSubmissionEntity current = getCurrent(submitForm.getSubmissionId());
        if (current == null) {
            return ResponseDTO.error(UserErrorCode.DATA_NOT_EXIST);
        }
        if (!Objects.equals(current.getVersion(), submitForm.getVersion())) {
            return ResponseDTO.userErrorParam("数据已更新，请刷新后重试");
        }
        if (!Objects.equals(current.getStatus(), BidSubmissionStatusEnum.QUALIFIED.getCode())
                && !Objects.equals(current.getStatus(), BidSubmissionStatusEnum.SUBMITTED.getCode())) {
            return ResponseDTO.userErrorParam("当前投标状态不允许提交");
        }

        ResponseDTO<String> lotValidateResult = validateBidWindow(current.getLotId());
        if (!lotValidateResult.getOk()) {
            return lotValidateResult;
        }

        LocalDateTime now = LocalDateTime.now();
        Integer nextVersionNo = current.getLatestVersionNo() == null ? 1 : current.getLatestVersionNo() + 1;
        String receiptNo = generateReceiptNo(current.getSubmissionId(), nextVersionNo, now);

        BidSubmissionEntity updateEntity = new BidSubmissionEntity();
        updateEntity.setSubmissionId(current.getSubmissionId());
        updateEntity.setVersion(submitForm.getVersion());
        updateEntity.setStatus(BidSubmissionStatusEnum.SUBMITTED.getCode());
        updateEntity.setLatestVersionNo(nextVersionNo);
        updateEntity.setLatestSubmitTime(now);
        updateEntity.setReceiptNo(receiptNo);
        updateEntity.setUpdateUserId(SmartRequestUtil.getRequestUserId());
        Integer updateCount = bidSubmissionDao.updateWithVersion(updateEntity);
        if (updateCount == null || updateCount == 0) {
            return ResponseDTO.userErrorParam("数据已更新，请刷新后重试");
        }

        bidSubmissionDao.disableEffectiveVersions(current.getSubmissionId());
        BidSubmissionVersionEntity versionEntity = new BidSubmissionVersionEntity();
        versionEntity.setSubmissionId(current.getSubmissionId());
        versionEntity.setVersionNo(nextVersionNo);
        versionEntity.setReceiptNo(receiptNo);
        versionEntity.setSubmitTime(now);
        versionEntity.setSubmitUserId(SmartRequestUtil.getRequestUserId());
        versionEntity.setPriceAmount(submitForm.getPriceAmount());
        versionEntity.setContactName(StringUtils.trimToEmpty(submitForm.getContactName()));
        versionEntity.setContactPhone(StringUtils.trimToEmpty(submitForm.getContactPhone()));
        versionEntity.setFileManifestJson(submitForm.getFileManifestJson());
        versionEntity.setEffective(Boolean.TRUE);
        versionEntity.setDeletedFlag(Boolean.FALSE);
        versionEntity.setCreateUserId(SmartRequestUtil.getRequestUserId());
        bidSubmissionDao.insertSubmissionVersion(versionEntity);

        bidWorkflowHistoryService.recordSubmissionAction(current.getSubmissionId(), current.getProjectId(), current.getLotId(),
                current.getStatus(), BidSubmissionStatusEnum.SUBMITTED.getCode(), "submit-bid", submitForm.getRemark(), SmartRequestUtil.getRequestUser(), versionEntity);
        return ResponseDTO.ok();
    }

    /**
     * 撤回投标
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> withdraw(BidSubmissionActionForm actionForm) {
        BidSubmissionEntity current = getCurrent(actionForm.getSubmissionId());
        if (current == null) {
            return ResponseDTO.error(UserErrorCode.DATA_NOT_EXIST);
        }
        if (!Objects.equals(current.getVersion(), actionForm.getVersion())) {
            return ResponseDTO.userErrorParam("数据已更新，请刷新后重试");
        }
        if (!Objects.equals(current.getStatus(), BidSubmissionStatusEnum.SUBMITTED.getCode())) {
            return ResponseDTO.userErrorParam("只有已投标记录才能撤回");
        }

        ResponseDTO<String> lotValidateResult = validateBidWindow(current.getLotId());
        if (!lotValidateResult.getOk()) {
            return lotValidateResult;
        }

        BidSubmissionEntity updateEntity = new BidSubmissionEntity();
        updateEntity.setSubmissionId(current.getSubmissionId());
        updateEntity.setVersion(actionForm.getVersion());
        updateEntity.setStatus(BidSubmissionStatusEnum.WITHDRAWN.getCode());
        updateEntity.setWithdrawTime(LocalDateTime.now());
        updateEntity.setWithdrawReason(StringUtils.trimToEmpty(actionForm.getRemark()));
        updateEntity.setUpdateUserId(SmartRequestUtil.getRequestUserId());
        Integer updateCount = bidSubmissionDao.updateWithVersion(updateEntity);
        if (updateCount == null || updateCount == 0) {
            return ResponseDTO.userErrorParam("数据已更新，请刷新后重试");
        }

        bidWorkflowHistoryService.recordSubmissionAction(current.getSubmissionId(), current.getProjectId(), current.getLotId(),
                current.getStatus(), BidSubmissionStatusEnum.WITHDRAWN.getCode(), "withdraw-bid", actionForm.getRemark(), SmartRequestUtil.getRequestUser(), updateEntity);
        return ResponseDTO.ok();
    }

    private ResponseDTO<String> validateCreateForm(BidSubmissionCreateForm form, BidRegistrationEntity registration) {
        if (registration == null || Boolean.TRUE.equals(registration.getDeletedFlag())) {
            return ResponseDTO.userErrorParam("报名记录不存在");
        }
        if (!Objects.equals(registration.getProjectId(), form.getProjectId())
                || !Objects.equals(registration.getLotId(), form.getLotId())) {
            return ResponseDTO.userErrorParam("报名记录与项目或标段不一致");
        }
        if (!Objects.equals(registration.getStatus(), BidRegistrationStatusEnum.QUALIFIED.getCode())) {
            return ResponseDTO.userErrorParam("只有资格通过报名才能创建投标记录");
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
        if (!Objects.equals(lot.getStatus(), BidLotStatusEnum.BIDDING.getCode())) {
            return ResponseDTO.userErrorParam("只有投标中的标段才允许创建投标记录");
        }
        return ResponseDTO.ok();
    }

    private ResponseDTO<String> validateBidWindow(Long lotId) {
        BidLotEntity lot = bidLotDao.selectById(lotId);
        if (lot == null || Boolean.TRUE.equals(lot.getDeletedFlag())) {
            return ResponseDTO.userErrorParam("所属标段不存在");
        }
        if (!Objects.equals(lot.getStatus(), BidLotStatusEnum.BIDDING.getCode())) {
            return ResponseDTO.userErrorParam("只有投标中的标段才允许投标操作");
        }

        LocalDateTime now = LocalDateTime.now();
        if (lot.getBidStartTime() != null && now.isBefore(lot.getBidStartTime())) {
            return ResponseDTO.userErrorParam("投标尚未开始");
        }
        if (lot.getBidEndTime() != null && now.isAfter(lot.getBidEndTime())) {
            return ResponseDTO.userErrorParam("投标已截止");
        }
        return ResponseDTO.ok();
    }

    private boolean existsActiveSubmission(BidRegistrationEntity registration) {
        Long count = bidSubmissionDao.selectCount(Wrappers.<BidSubmissionEntity>lambdaQuery()
                .eq(BidSubmissionEntity::getLotId, registration.getLotId())
                .eq(BidSubmissionEntity::getSupplierCreditCode, registration.getSupplierCreditCode())
                .eq(BidSubmissionEntity::getDeletedFlag, Boolean.FALSE));
        return count != null && count > 0;
    }

    private BidSubmissionEntity getCurrent(Long submissionId) {
        BidSubmissionEntity current = bidSubmissionDao.selectById(submissionId);
        if (current == null || Boolean.TRUE.equals(current.getDeletedFlag())) {
            return null;
        }
        return current;
    }

    private String generateReceiptNo(Long submissionId, Integer versionNo, LocalDateTime submitTime) {
        return "TB" + submitTime.format(RECEIPT_TIME_FORMATTER) + "-" + submissionId + "-" + versionNo;
    }

    private void fillAllowedActions(BidSubmissionVO submissionVO) {
        if (Objects.equals(submissionVO.getStatus(), BidSubmissionStatusEnum.QUALIFIED.getCode())) {
            submissionVO.setAllowedActions(List.of("submit-bid"));
            return;
        }
        if (Objects.equals(submissionVO.getStatus(), BidSubmissionStatusEnum.SUBMITTED.getCode())) {
            submissionVO.setAllowedActions(List.of("submit-bid", "withdraw-bid"));
            return;
        }
        submissionVO.setAllowedActions(Collections.emptyList());
    }

    private void fillDetail(BidSubmissionVO submissionVO) {
        fillAllowedActions(submissionVO);
        submissionVO.setAttachments(bidAttachmentService.listByBusiness(
                BidBusinessTypeEnum.SUBMISSION_VERSION.getCode(),
                submissionVO.getLatestSubmissionVersionId(),
                "/bid/submissions/" + submissionVO.getSubmissionId()));
    }
}
