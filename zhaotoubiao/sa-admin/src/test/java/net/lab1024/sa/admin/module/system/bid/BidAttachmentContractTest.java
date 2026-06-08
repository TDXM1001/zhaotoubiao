package net.lab1024.sa.admin.module.system.bid;

import net.lab1024.sa.admin.module.system.bid.attachment.domain.vo.BidAttachmentVO;
import net.lab1024.sa.admin.module.system.bid.submission.domain.vo.BidSubmissionVO;
import net.lab1024.sa.admin.module.system.bid.tender.domain.vo.BidTenderVO;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 招投标附件摘要合同测试。
 *
 * @author Codex
 * @date 2026-06-08
 */
class BidAttachmentContractTest {

    @Test
    void 招标和投标详情应返回附件摘要列表() throws Exception {
        assertEquals(List.class, BidTenderVO.class.getDeclaredField("attachments").getType());
        assertEquals(List.class, BidSubmissionVO.class.getDeclaredField("attachments").getType());
    }

    @Test
    void 附件摘要应隐藏文件存储细节并保留下载预览入口() throws Exception {
        assertField("attachmentId", Long.class);
        assertField("businessType", String.class);
        assertField("businessId", Long.class);
        assertField("fileId", Long.class);
        assertField("fileName", String.class);
        assertField("fileSize", Long.class);
        assertField("fileCategory", String.class);
        assertField("versionNo", Integer.class);
        assertField("mainFlag", Boolean.class);
        assertField("downloadUrl", String.class);
        assertField("previewUrl", String.class);
        assertField("uploadTime", LocalDateTime.class);
        assertField("uploaderName", String.class);
    }

    @Test
    void 附件摘要应生成业务资源入口而不是暴露文件模块直链() {
        BidAttachmentVO attachmentVO = new BidAttachmentVO();
        attachmentVO.setAttachmentId(20L);

        attachmentVO.fillBusinessUrls("/bid/submissions/10");

        assertEquals("/bid/submissions/10/attachments/20/download", attachmentVO.getDownloadUrl());
        assertEquals("/bid/submissions/10/attachments/20/preview-url", attachmentVO.getPreviewUrl());
    }

    private void assertField(String fieldName, Class<?> fieldType) throws NoSuchFieldException {
        Field field = BidAttachmentVO.class.getDeclaredField(fieldName);
        assertEquals(fieldType, field.getType());
    }
}
