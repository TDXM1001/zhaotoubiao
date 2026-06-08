package net.lab1024.sa.admin.module.system.bid.attachment.service;

import jakarta.annotation.Resource;
import net.lab1024.sa.admin.module.system.bid.attachment.dao.BidAttachmentDao;
import net.lab1024.sa.admin.module.system.bid.attachment.domain.entity.BidAttachmentEntity;
import net.lab1024.sa.admin.module.system.bid.attachment.domain.form.BidAttachmentCreateForm;
import net.lab1024.sa.admin.module.system.bid.attachment.domain.vo.BidAttachmentVO;
import net.lab1024.sa.base.common.code.UserErrorCode;
import net.lab1024.sa.base.common.domain.ResponseDTO;
import net.lab1024.sa.base.common.util.SmartRequestUtil;
import net.lab1024.sa.base.module.support.file.dao.FileDao;
import net.lab1024.sa.base.module.support.file.domain.entity.FileEntity;
import net.lab1024.sa.base.module.support.file.domain.vo.FileDownloadVO;
import net.lab1024.sa.base.module.support.file.service.FileService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

/**
 * 招投标附件 Service
 *
 * @author Codex
 * @date 2026-06-08
 */
@Service
public class BidAttachmentService {

    @Resource
    private BidAttachmentDao bidAttachmentDao;

    @Resource
    private FileService fileService;

    @Resource
    private FileDao fileDao;

    /**
     * 查询业务对象附件摘要
     */
    public List<BidAttachmentVO> listByBusiness(String businessType, Long businessId, String businessResourcePath) {
        if (businessType == null || businessId == null) {
            return Collections.emptyList();
        }

        List<BidAttachmentVO> attachmentList = bidAttachmentDao.listByBusiness(businessType, businessId);
        if (attachmentList == null || attachmentList.isEmpty()) {
            return Collections.emptyList();
        }
        attachmentList.forEach(attachmentVO -> attachmentVO.fillBusinessUrls(businessResourcePath));
        return attachmentList;
    }

    /**
     * 下载业务对象附件
     */
    public ResponseDTO<FileDownloadVO> download(String businessType, Long businessId, Long attachmentId, String userAgent) {
        BidAttachmentVO attachmentVO = getAttachment(businessType, businessId, attachmentId);
        if (attachmentVO == null) {
            return ResponseDTO.error(UserErrorCode.DATA_NOT_EXIST);
        }
        return fileService.getDownloadFile(attachmentVO.getFileKey(), userAgent);
    }

    /**
     * 查询业务对象附件预览地址
     */
    public ResponseDTO<String> getPreviewUrl(String businessType, Long businessId, Long attachmentId) {
        BidAttachmentVO attachmentVO = getAttachment(businessType, businessId, attachmentId);
        if (attachmentVO == null) {
            return ResponseDTO.error(UserErrorCode.DATA_NOT_EXIST);
        }
        return fileService.getFileUrl(attachmentVO.getFileKey());
    }

    /**
     * 关联已上传文件到招投标业务对象
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> create(String businessType,
                                      Long businessId,
                                      Long projectId,
                                      Long lotId,
                                      BidAttachmentCreateForm createForm) {
        if (businessType == null || businessId == null) {
            return ResponseDTO.userErrorParam("附件业务对象不能为空");
        }
        FileEntity fileEntity = fileDao.selectById(createForm.getFileId());
        if (fileEntity == null) {
            return ResponseDTO.userErrorParam("文件不存在");
        }

        Long existsCount = bidAttachmentDao.countByBusinessAndFile(businessType, businessId, createForm.getFileId());
        if (existsCount != null && existsCount > 0) {
            return ResponseDTO.userErrorParam("当前文件已关联到该业务对象");
        }

        BidAttachmentEntity entity = new BidAttachmentEntity();
        entity.setBusinessType(businessType);
        entity.setBusinessId(businessId);
        entity.setProjectId(projectId);
        entity.setLotId(lotId);
        entity.setFileId(createForm.getFileId());
        entity.setFileCategory(createForm.getFileCategory());
        entity.setVersionNo(createForm.getVersionNo());
        entity.setSortNo(createForm.getSortNo() == null ? 0 : createForm.getSortNo());
        entity.setMainFlag(Boolean.TRUE.equals(createForm.getMainFlag()));
        entity.setDeletedFlag(Boolean.FALSE);
        entity.setCreateUserId(SmartRequestUtil.getRequestUserId());
        bidAttachmentDao.insert(entity);
        return ResponseDTO.ok();
    }

    private BidAttachmentVO getAttachment(String businessType, Long businessId, Long attachmentId) {
        if (businessType == null || businessId == null || attachmentId == null) {
            return null;
        }
        return bidAttachmentDao.getByBusinessAndAttachment(businessType, businessId, attachmentId);
    }
}
