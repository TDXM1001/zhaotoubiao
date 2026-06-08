package net.lab1024.sa.admin.module.system.bid.portal.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import net.lab1024.sa.admin.module.system.bid.attachment.service.BidAttachmentService;
import net.lab1024.sa.admin.module.system.bid.common.constant.BidBusinessTypeEnum;
import net.lab1024.sa.admin.module.system.bid.portal.dao.BidPortalDao;
import net.lab1024.sa.admin.module.system.bid.portal.domain.form.BidPortalProjectQueryForm;
import net.lab1024.sa.admin.module.system.bid.portal.domain.form.BidPortalRegistrationCreateForm;
import net.lab1024.sa.admin.module.system.bid.portal.domain.form.BidPortalSubmissionActionForm;
import net.lab1024.sa.admin.module.system.bid.portal.domain.form.BidPortalSubmissionCreateForm;
import net.lab1024.sa.admin.module.system.bid.portal.domain.form.BidPortalSubmissionSubmitForm;
import net.lab1024.sa.admin.module.system.bid.portal.domain.vo.BidPortalLotVO;
import net.lab1024.sa.admin.module.system.bid.portal.domain.vo.BidPortalProjectVO;
import net.lab1024.sa.admin.module.system.bid.portal.domain.vo.BidPortalRegistrationVO;
import net.lab1024.sa.admin.module.system.bid.portal.domain.vo.BidPortalRequestUser;
import net.lab1024.sa.admin.module.system.bid.portal.domain.vo.BidPortalSubmissionVO;
import net.lab1024.sa.admin.module.system.bid.registration.domain.form.BidRegistrationAddForm;
import net.lab1024.sa.admin.module.system.bid.registration.service.BidRegistrationService;
import net.lab1024.sa.admin.module.system.bid.submission.domain.form.BidSubmissionActionForm;
import net.lab1024.sa.admin.module.system.bid.submission.domain.form.BidSubmissionCreateForm;
import net.lab1024.sa.admin.module.system.bid.submission.domain.form.BidSubmissionSubmitForm;
import net.lab1024.sa.admin.module.system.bid.submission.service.BidSubmissionService;
import net.lab1024.sa.base.common.code.UserErrorCode;
import net.lab1024.sa.base.common.domain.PageResult;
import net.lab1024.sa.base.common.domain.ResponseDTO;
import net.lab1024.sa.base.common.util.SmartBeanUtil;
import net.lab1024.sa.base.common.util.SmartPageUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 供应商门户 Service
 *
 * @author Codex
 * @date 2026-06-08
 */
@Service
public class BidPortalService {

    @Resource
    private BidPortalDao bidPortalDao;

    @Resource
    private BidRegistrationService bidRegistrationService;

    @Resource
    private BidSubmissionService bidSubmissionService;

    @Resource
    private BidAttachmentService bidAttachmentService;

    /**
     * 查询门户可见项目
     */
    public PageResult<BidPortalProjectVO> queryProjectPage(BidPortalProjectQueryForm queryForm) {
        Page<?> page = SmartPageUtil.convert2PageQuery(queryForm);
        List<BidPortalProjectVO> list = bidPortalDao.queryVisibleProjectPage(page, queryForm);
        return SmartPageUtil.convert2PageResult(page, list);
    }

    /**
     * 查询门户可见项目详情
     */
    public ResponseDTO<BidPortalProjectVO> getProjectDetail(Long projectId) {
        BidPortalProjectVO projectVO = bidPortalDao.getVisibleProject(projectId);
        if (projectVO == null) {
            return ResponseDTO.error(UserErrorCode.DATA_NOT_EXIST);
        }
        List<BidPortalLotVO> lotList = bidPortalDao.listVisibleLots(projectId);
        if (lotList != null) {
            lotList.forEach(lotVO -> lotVO.setTenderAttachments(bidAttachmentService.listByBusiness(
                    BidBusinessTypeEnum.TENDER_VERSION.getCode(), lotVO.getTenderVersionId(), null)));
        }
        projectVO.setLots(lotList);
        return ResponseDTO.ok(projectVO);
    }

    /**
     * 门户提交报名
     */
    public ResponseDTO<String> createRegistration(BidPortalRegistrationCreateForm createForm, BidPortalRequestUser requestUser) {
        ResponseDTO<String> loginCheck = checkLogin(requestUser);
        if (!loginCheck.getOk()) {
            return loginCheck;
        }
        fillSupplier(createForm, requestUser);
        BidRegistrationAddForm addForm = SmartBeanUtil.copy(createForm, BidRegistrationAddForm.class);
        return bidRegistrationService.add(addForm);
    }

    /**
     * 查询当前供应商报名详情
     */
    public ResponseDTO<BidPortalRegistrationVO> getRegistration(Long registrationId, BidPortalRequestUser requestUser) {
        ResponseDTO<String> loginCheck = checkLogin(requestUser);
        if (!loginCheck.getOk()) {
            return ResponseDTO.error(loginCheck);
        }
        BidPortalRegistrationVO registrationVO = bidPortalDao.getRegistration(registrationId, requestUser.getSupplierCreditCode());
        if (registrationVO == null) {
            return ResponseDTO.error(UserErrorCode.DATA_NOT_EXIST);
        }
        return ResponseDTO.ok(registrationVO);
    }

    /**
     * 门户创建投标主记录
     */
    public ResponseDTO<String> createSubmission(BidPortalSubmissionCreateForm createForm, BidPortalRequestUser requestUser) {
        ResponseDTO<String> loginCheck = checkLogin(requestUser);
        if (!loginCheck.getOk()) {
            return loginCheck;
        }
        createForm.setSupplierCreditCode(requestUser.getSupplierCreditCode());
        ResponseDTO<BidPortalRegistrationVO> registrationResult = getRegistration(createForm.getRegistrationId(), requestUser);
        if (!registrationResult.getOk()) {
            return ResponseDTO.error(registrationResult);
        }

        BidSubmissionCreateForm internalForm = SmartBeanUtil.copy(createForm, BidSubmissionCreateForm.class);
        return bidSubmissionService.create(internalForm);
    }

    /**
     * 查询当前供应商投标详情
     */
    public ResponseDTO<BidPortalSubmissionVO> getSubmission(Long submissionId, BidPortalRequestUser requestUser) {
        ResponseDTO<String> loginCheck = checkLogin(requestUser);
        if (!loginCheck.getOk()) {
            return ResponseDTO.error(loginCheck);
        }
        BidPortalSubmissionVO submissionVO = bidPortalDao.getSubmission(submissionId, requestUser.getSupplierCreditCode());
        if (submissionVO == null) {
            return ResponseDTO.error(UserErrorCode.DATA_NOT_EXIST);
        }
        submissionVO.setAttachments(bidAttachmentService.listByBusiness(
                BidBusinessTypeEnum.SUBMISSION_VERSION.getCode(), submissionVO.getLatestSubmissionVersionId(), null));
        return ResponseDTO.ok(submissionVO);
    }

    /**
     * 门户提交投标
     */
    public ResponseDTO<String> submitBid(BidPortalSubmissionSubmitForm submitForm, BidPortalRequestUser requestUser) {
        ResponseDTO<String> loginCheck = checkLogin(requestUser);
        if (!loginCheck.getOk()) {
            return loginCheck;
        }
        submitForm.setSupplierCreditCode(requestUser.getSupplierCreditCode());
        ResponseDTO<BidPortalSubmissionVO> submissionResult = getSubmission(submitForm.getSubmissionId(), requestUser);
        if (!submissionResult.getOk()) {
            return ResponseDTO.error(submissionResult);
        }

        BidSubmissionSubmitForm internalForm = SmartBeanUtil.copy(submitForm, BidSubmissionSubmitForm.class);
        return bidSubmissionService.submitBid(internalForm);
    }

    /**
     * 门户撤回投标
     */
    public ResponseDTO<String> withdrawBid(BidPortalSubmissionActionForm actionForm, BidPortalRequestUser requestUser) {
        ResponseDTO<String> loginCheck = checkLogin(requestUser);
        if (!loginCheck.getOk()) {
            return loginCheck;
        }
        actionForm.setSupplierCreditCode(requestUser.getSupplierCreditCode());
        ResponseDTO<BidPortalSubmissionVO> submissionResult = getSubmission(actionForm.getSubmissionId(), requestUser);
        if (!submissionResult.getOk()) {
            return ResponseDTO.error(submissionResult);
        }

        BidSubmissionActionForm internalForm = SmartBeanUtil.copy(actionForm, BidSubmissionActionForm.class);
        return bidSubmissionService.withdraw(internalForm);
    }

    private void fillSupplier(BidPortalRegistrationCreateForm createForm, BidPortalRequestUser requestUser) {
        createForm.setSupplierEnterpriseId(requestUser.getSupplierEnterpriseId());
        createForm.setSupplierNameSnapshot(requestUser.getSupplierName());
        createForm.setSupplierCreditCode(requestUser.getSupplierCreditCode());
        if (StringUtils.isBlank(createForm.getContactName())) {
            createForm.setContactName(requestUser.getContactName());
        }
        if (StringUtils.isBlank(createForm.getContactPhone())) {
            createForm.setContactPhone(requestUser.getContactPhone());
        }
    }

    private ResponseDTO<String> checkLogin(BidPortalRequestUser requestUser) {
        if (requestUser == null) {
            return ResponseDTO.error(UserErrorCode.LOGIN_STATE_INVALID);
        }
        return ResponseDTO.ok();
    }
}
