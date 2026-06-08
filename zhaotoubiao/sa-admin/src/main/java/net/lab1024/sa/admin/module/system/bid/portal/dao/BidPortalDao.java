package net.lab1024.sa.admin.module.system.bid.portal.dao;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import net.lab1024.sa.admin.module.system.bid.portal.domain.form.BidPortalProjectQueryForm;
import net.lab1024.sa.admin.module.system.bid.portal.domain.vo.BidPortalLotVO;
import net.lab1024.sa.admin.module.system.bid.portal.domain.vo.BidPortalProjectVO;
import net.lab1024.sa.admin.module.system.bid.portal.domain.vo.BidPortalRegistrationVO;
import net.lab1024.sa.admin.module.system.bid.portal.domain.vo.BidPortalResultVO;
import net.lab1024.sa.admin.module.system.bid.portal.domain.vo.BidPortalSubmissionVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 供应商门户 Dao
 *
 * @author Codex
 * @date 2026-06-08
 */
@Mapper
public interface BidPortalDao {

    /**
     * 查询门户可见项目
     */
    List<BidPortalProjectVO> queryVisibleProjectPage(Page<?> page, @Param("queryForm") BidPortalProjectQueryForm queryForm);

    /**
     * 查询门户可见项目详情
     */
    BidPortalProjectVO getVisibleProject(@Param("projectId") Long projectId);

    /**
     * 查询门户可见标段
     */
    List<BidPortalLotVO> listVisibleLots(@Param("projectId") Long projectId);

    /**
     * 查询当前供应商报名
     */
    BidPortalRegistrationVO getRegistration(@Param("registrationId") Long registrationId,
                                            @Param("supplierCreditCode") String supplierCreditCode);

    /**
     * 查询当前供应商投标
     */
    BidPortalSubmissionVO getSubmission(@Param("submissionId") Long submissionId,
                                        @Param("supplierCreditCode") String supplierCreditCode);

    /**
     * 查询当前供应商标段结果
     */
    BidPortalResultVO getLotResult(@Param("lotId") Long lotId,
                                   @Param("supplierCreditCode") String supplierCreditCode);
}
