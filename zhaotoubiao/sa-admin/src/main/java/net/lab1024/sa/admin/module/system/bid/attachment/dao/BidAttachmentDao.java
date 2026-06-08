package net.lab1024.sa.admin.module.system.bid.attachment.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.admin.module.system.bid.attachment.domain.entity.BidAttachmentEntity;
import net.lab1024.sa.admin.module.system.bid.attachment.domain.vo.BidAttachmentVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 招投标附件 Dao
 *
 * @author Codex
 * @date 2026-06-08
 */
@Mapper
public interface BidAttachmentDao extends BaseMapper<BidAttachmentEntity> {

    /**
     * 查询业务对象附件摘要
     */
    List<BidAttachmentVO> listByBusiness(@Param("businessType") String businessType,
                                         @Param("businessId") Long businessId);

    /**
     * 查询业务对象内指定附件
     */
    BidAttachmentVO getByBusinessAndAttachment(@Param("businessType") String businessType,
                                               @Param("businessId") Long businessId,
                                               @Param("attachmentId") Long attachmentId);
}
