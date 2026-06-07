package net.lab1024.sa.admin.module.system.bid.registration.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import net.lab1024.sa.admin.module.system.bid.registration.domain.entity.BidRegistrationEntity;
import net.lab1024.sa.admin.module.system.bid.registration.domain.form.BidRegistrationQueryForm;
import net.lab1024.sa.admin.module.system.bid.registration.domain.vo.BidRegistrationVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 供应商报名 Dao
 *
 * @author Codex
 * @date 2026-06-07
 */
@Mapper
public interface BidRegistrationDao extends BaseMapper<BidRegistrationEntity> {

    /**
     * 分页查询报名记录
     */
    List<BidRegistrationVO> queryPage(Page<?> page, @Param("queryForm") BidRegistrationQueryForm queryForm);

    /**
     * 查询报名详情
     */
    BidRegistrationVO getDetail(@Param("registrationId") Long registrationId);

    /**
     * 按版本号更新
     */
    Integer updateWithVersion(BidRegistrationEntity entity);
}
