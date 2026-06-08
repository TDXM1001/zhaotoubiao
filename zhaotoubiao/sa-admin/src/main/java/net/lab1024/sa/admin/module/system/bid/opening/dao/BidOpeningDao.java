package net.lab1024.sa.admin.module.system.bid.opening.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import net.lab1024.sa.admin.module.system.bid.opening.domain.entity.BidOpeningEntity;
import net.lab1024.sa.admin.module.system.bid.opening.domain.form.BidOpeningQueryForm;
import net.lab1024.sa.admin.module.system.bid.opening.domain.vo.BidOpeningItemVO;
import net.lab1024.sa.admin.module.system.bid.opening.domain.vo.BidOpeningVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 开标 Dao
 *
 * @author Codex
 * @date 2026-06-08
 */
@Mapper
public interface BidOpeningDao extends BaseMapper<BidOpeningEntity> {

    /**
     * 分页查询开标记录
     */
    List<BidOpeningVO> queryPage(Page<?> page, @Param("queryForm") BidOpeningQueryForm queryForm);

    /**
     * 查询开标详情
     */
    BidOpeningVO getDetail(@Param("openingId") Long openingId);

    /**
     * 查询标段开标记录
     */
    BidOpeningVO getByLotId(@Param("lotId") Long lotId);

    /**
     * 查询开标明细
     */
    List<BidOpeningItemVO> listItems(@Param("openingId") Long openingId);

    /**
     * 统计开标明细数
     */
    Long countItems(@Param("openingId") Long openingId);

    /**
     * 按版本号更新开标记录
     */
    Integer updateWithVersion(BidOpeningEntity entity);

    /**
     * 从有效投标版本生成开标明细
     */
    Integer insertItemsFromSubmissions(@Param("openingId") Long openingId,
                                       @Param("lotId") Long lotId);

    /**
     * 将已生成开标明细的投标推进为已开标
     */
    Integer markSubmissionsOpenedByOpening(@Param("openingId") Long openingId,
                                           @Param("updateUserId") Long updateUserId);
}
