package net.lab1024.sa.admin.module.system.bid.submission.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import net.lab1024.sa.admin.module.system.bid.submission.domain.entity.BidSubmissionEntity;
import net.lab1024.sa.admin.module.system.bid.submission.domain.entity.BidSubmissionVersionEntity;
import net.lab1024.sa.admin.module.system.bid.submission.domain.form.BidSubmissionQueryForm;
import net.lab1024.sa.admin.module.system.bid.submission.domain.vo.BidSubmissionVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 投标 Dao
 *
 * @author Codex
 * @date 2026-06-08
 */
@Mapper
public interface BidSubmissionDao extends BaseMapper<BidSubmissionEntity> {

    /**
     * 分页查询投标
     */
    List<BidSubmissionVO> queryPage(Page<?> page, @Param("queryForm") BidSubmissionQueryForm queryForm);

    /**
     * 查询投标详情
     */
    BidSubmissionVO getDetail(@Param("submissionId") Long submissionId);

    /**
     * 按报名查询投标主记录
     */
    BidSubmissionVO getByRegistrationId(@Param("registrationId") Long registrationId);

    /**
     * 按版本号更新投标主记录
     */
    Integer updateWithVersion(BidSubmissionEntity entity);

    /**
     * 将既有有效投标版本置为无效
     */
    Integer disableEffectiveVersions(@Param("submissionId") Long submissionId);

    /**
     * 插入投标版本
     */
    Integer insertSubmissionVersion(BidSubmissionVersionEntity entity);
}
