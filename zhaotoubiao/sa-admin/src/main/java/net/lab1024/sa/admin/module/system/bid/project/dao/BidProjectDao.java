package net.lab1024.sa.admin.module.system.bid.project.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import net.lab1024.sa.admin.module.system.bid.project.domain.entity.BidProjectEntity;
import net.lab1024.sa.admin.module.system.bid.project.domain.form.BidProjectQueryForm;
import net.lab1024.sa.admin.module.system.bid.project.domain.vo.BidProjectOptionVO;
import net.lab1024.sa.admin.module.system.bid.project.domain.vo.BidProjectVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 招标项目 Dao
 *
 * @author Codex
 * @date 2026-06-07
 */
@Mapper
public interface BidProjectDao extends BaseMapper<BidProjectEntity> {

    /**
     * 分页查询项目
     */
    List<BidProjectVO> queryPage(Page<?> page, @Param("queryForm") BidProjectQueryForm queryForm);

    /**
     * 查询项目详情
     */
    BidProjectVO getDetail(@Param("projectId") Long projectId);

    /**
     * 查询项目下拉
     */
    List<BidProjectOptionVO> queryList(@Param("status") String status);

    /**
     * 统计项目下有效标段数
     */
    Long countValidLotByProjectId(@Param("projectId") Long projectId);

    /**
     * 按版本号更新
     */
    Integer updateWithVersion(BidProjectEntity entity);
}
