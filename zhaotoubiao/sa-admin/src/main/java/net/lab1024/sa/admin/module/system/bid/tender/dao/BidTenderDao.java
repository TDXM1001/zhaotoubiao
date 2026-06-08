package net.lab1024.sa.admin.module.system.bid.tender.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import net.lab1024.sa.admin.module.system.bid.tender.domain.entity.BidTenderVersionEntity;
import net.lab1024.sa.admin.module.system.bid.tender.domain.form.BidTenderQueryForm;
import net.lab1024.sa.admin.module.system.bid.tender.domain.vo.BidTenderVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 招标文件 Dao
 *
 * @author Codex
 * @date 2026-06-08
 */
@Mapper
public interface BidTenderDao extends BaseMapper<BidTenderVersionEntity> {

    /**
     * 分页查询招标文件版本
     */
    List<BidTenderVO> queryPage(Page<?> page, @Param("queryForm") BidTenderQueryForm queryForm);

    /**
     * 查询招标文件详情
     */
    BidTenderVO getDetail(@Param("tenderVersionId") Long tenderVersionId);

    /**
     * 查询标段当前有效招标文件版本
     */
    BidTenderVO getActiveByLotId(@Param("lotId") Long lotId);

    /**
     * 查询标段最大版本号
     */
    Integer selectMaxVersionNoByLotId(@Param("lotId") Long lotId);

    /**
     * 按版本号更新
     */
    Integer updateWithVersion(BidTenderVersionEntity entity);

    /**
     * 将标段当前有效版本置为已替代
     */
    Integer supersedeCurrentByLotId(@Param("lotId") Long lotId,
                                    @Param("excludeTenderVersionId") Long excludeTenderVersionId,
                                    @Param("updateUserId") Long updateUserId);
}
