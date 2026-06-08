package net.lab1024.sa.admin.module.system.bid.attachment.service;

import jakarta.annotation.Resource;
import net.lab1024.sa.admin.module.system.bid.attachment.dao.BidAttachmentDao;
import net.lab1024.sa.admin.module.system.bid.attachment.domain.vo.BidAttachmentVO;
import net.lab1024.sa.base.common.code.UserErrorCode;
import net.lab1024.sa.base.common.domain.ResponseDTO;
import net.lab1024.sa.base.module.support.file.domain.vo.FileDownloadVO;
import net.lab1024.sa.base.module.support.file.service.FileService;
import org.springframework.stereotype.Service;

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

    private BidAttachmentVO getAttachment(String businessType, Long businessId, Long attachmentId) {
        if (businessType == null || businessId == null || attachmentId == null) {
            return null;
        }
        return bidAttachmentDao.getByBusinessAndAttachment(businessType, businessId, attachmentId);
    }
}
