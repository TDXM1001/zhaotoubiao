package net.lab1024.sa.admin.module.system.bid.award.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import net.lab1024.sa.admin.module.system.bid.award.domain.entity.BidAwardEntity;
import net.lab1024.sa.admin.module.system.bid.award.domain.form.BidAwardQueryForm;
import net.lab1024.sa.admin.module.system.bid.award.domain.vo.BidAwardVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

/**
 * 定标 Dao
 *
 * @author Codex
 * @date 2026-06-08
 */
@Mapper
public interface BidAwardDao extends BaseMapper<BidAwardEntity> {

    /**
     * 分页查询定标记录
     */
    List<BidAwardVO> queryPage(Page<?> page, @Param("queryForm") BidAwardQueryForm queryForm);

    /**
     * 查询定标详情
     */
    BidAwardVO getDetail(@Param("awardId") Long awardId);

    /**
     * 查询标段定标记录
     */
    BidAwardVO getByLotId(@Param("lotId") Long lotId);

    /**
     * 查询投标最新有效报价
     */
    BigDecimal getSubmissionLatestPrice(@Param("submissionId") Long submissionId);

    /**
     * 按版本号更新定标记录
     */
    Integer updateWithVersion(BidAwardEntity entity);
}
