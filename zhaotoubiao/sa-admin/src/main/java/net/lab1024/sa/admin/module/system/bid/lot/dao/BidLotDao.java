package net.lab1024.sa.admin.module.system.bid.lot.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import net.lab1024.sa.admin.module.system.bid.lot.domain.entity.BidLotEntity;
import net.lab1024.sa.admin.module.system.bid.lot.domain.form.BidLotQueryForm;
import net.lab1024.sa.admin.module.system.bid.lot.domain.vo.BidLotVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 标段 Dao
 *
 * @author Codex
 * @date 2026-06-07
 */
@Mapper
public interface BidLotDao extends BaseMapper<BidLotEntity> {

    /**
     * 分页查询标段
     */
    List<BidLotVO> queryPage(Page<?> page, @Param("queryForm") BidLotQueryForm queryForm);

    /**
     * 查询标段详情
     */
    BidLotVO getDetail(@Param("lotId") Long lotId);

    /**
     * 查询项目下全部标段
     */
    List<BidLotVO> queryByProjectId(@Param("projectId") Long projectId);

    /**
     * 按版本号更新
     */
    Integer updateWithVersion(BidLotEntity entity);

    /**
     * 项目发布时同步更新标段状态
     */
    Integer publishLotsByProjectId(@Param("projectId") Long projectId,
                                   @Param("updateUserId") Long updateUserId,
                                   @Param("fromStatus") String fromStatus,
                                   @Param("toStatus") String toStatus);
}
