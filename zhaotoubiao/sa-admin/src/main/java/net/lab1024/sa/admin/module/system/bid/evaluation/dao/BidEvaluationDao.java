package net.lab1024.sa.admin.module.system.bid.evaluation.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import net.lab1024.sa.admin.module.system.bid.evaluation.domain.entity.BidEvaluationEntity;
import net.lab1024.sa.admin.module.system.bid.evaluation.domain.form.BidEvaluationQueryForm;
import net.lab1024.sa.admin.module.system.bid.evaluation.domain.vo.BidEvaluationItemVO;
import net.lab1024.sa.admin.module.system.bid.evaluation.domain.vo.BidEvaluationVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 评标 Dao
 *
 * @author Codex
 * @date 2026-06-08
 */
@Mapper
public interface BidEvaluationDao extends BaseMapper<BidEvaluationEntity> {

    /**
     * 分页查询评标记录
     */
    List<BidEvaluationVO> queryPage(Page<?> page, @Param("queryForm") BidEvaluationQueryForm queryForm);

    /**
     * 查询评标详情
     */
    BidEvaluationVO getDetail(@Param("evaluationId") Long evaluationId);

    /**
     * 查询标段评标记录
     */
    BidEvaluationVO getByLotId(@Param("lotId") Long lotId);

    /**
     * 查询评标明细
     */
    List<BidEvaluationItemVO> listItems(@Param("evaluationId") Long evaluationId);

    /**
     * 统计评标明细数
     */
    Long countItems(@Param("evaluationId") Long evaluationId);

    /**
     * 按版本号更新评标记录
     */
    Integer updateWithVersion(BidEvaluationEntity entity);

    /**
     * 从开标明细生成评标明细
     */
    Integer insertItemsFromOpening(@Param("evaluationId") Long evaluationId,
                                   @Param("openingId") Long openingId);
}
