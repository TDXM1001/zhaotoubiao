package net.lab1024.sa.admin.module.system.bid.portal.service;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.extra.servlet.JakartaServletUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import net.lab1024.sa.admin.module.business.oa.enterprise.dao.EnterpriseDao;
import net.lab1024.sa.admin.module.business.oa.enterprise.domain.entity.EnterpriseEntity;
import net.lab1024.sa.admin.module.system.bid.portal.dao.BidPortalAccountDao;
import net.lab1024.sa.admin.module.system.bid.portal.domain.entity.BidPortalAccountEntity;
import net.lab1024.sa.admin.module.system.bid.portal.domain.form.BidPortalAuthLoginForm;
import net.lab1024.sa.admin.module.system.bid.portal.domain.form.BidPortalAuthRegisterForm;
import net.lab1024.sa.admin.module.system.bid.portal.domain.vo.BidPortalAuthVO;
import net.lab1024.sa.admin.module.system.bid.portal.domain.vo.BidPortalRequestUser;
import net.lab1024.sa.base.common.code.UserErrorCode;
import net.lab1024.sa.base.common.constant.RequestHeaderConst;
import net.lab1024.sa.base.common.domain.ResponseDTO;
import net.lab1024.sa.base.common.util.SmartBeanUtil;
import net.lab1024.sa.base.module.support.securityprotect.service.SecurityPasswordService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 供应商门户认证服务
 *
 * @author Codex
 * @date 2026-06-08
 */
@Service
public class BidPortalAuthService {

    private static final String LOGIN_ID_PREFIX = "BID_PORTAL_SUPPLIER:";

    @Resource
    private BidPortalAccountDao bidPortalAccountDao;

    @Resource
    private EnterpriseDao enterpriseDao;

    @Resource
    private SecurityPasswordService securityPasswordService;

    /**
     * 注册供应商门户账号
     */
    public ResponseDTO<BidPortalAuthVO> register(BidPortalAuthRegisterForm registerForm) {
        String loginName = normalizeLoginName(registerForm.getLoginName());
        String supplierCreditCode = normalizeSupplierCreditCode(registerForm.getSupplierCreditCode());
        String contactPhone = StringUtils.trim(registerForm.getContactPhone());

        ResponseDTO<String> passwordCheck = securityPasswordService.validatePasswordComplexity(registerForm.getPassword());
        if (!passwordCheck.getOk()) {
            return ResponseDTO.error(passwordCheck);
        }

        EnterpriseEntity enterpriseEntity = enterpriseDao.selectOne(new LambdaQueryWrapper<EnterpriseEntity>()
                .eq(EnterpriseEntity::getUnifiedSocialCreditCode, supplierCreditCode)
                .eq(EnterpriseEntity::getDeletedFlag, false)
                .last("LIMIT 1"));
        if (enterpriseEntity == null) {
            return ResponseDTO.userErrorParam("供应商企业档案不存在，请先联系采购方建档");
        }
        if (Boolean.TRUE.equals(enterpriseEntity.getDisabledFlag())) {
            return ResponseDTO.userErrorParam("供应商企业已禁用，无法注册门户账号");
        }
        if (!StringUtils.equals(StringUtils.trim(enterpriseEntity.getContactPhone()), contactPhone)) {
            return ResponseDTO.userErrorParam("联系人手机号与企业档案不一致");
        }

        if (existsByLoginName(loginName)) {
            return ResponseDTO.userErrorParam("登录账号已存在");
        }
        if (existsBySupplierCreditCode(supplierCreditCode)) {
            return ResponseDTO.userErrorParam("该供应商已注册门户账号");
        }

        BidPortalAccountEntity accountEntity = new BidPortalAccountEntity();
        accountEntity.setSupplierEnterpriseId(enterpriseEntity.getEnterpriseId());
        accountEntity.setSupplierName(enterpriseEntity.getEnterpriseName());
        accountEntity.setSupplierCreditCode(supplierCreditCode);
        accountEntity.setLoginName(loginName);
        accountEntity.setLoginPwd(SecurityPasswordService.getEncryptPwd(registerForm.getPassword()));
        accountEntity.setContactName(enterpriseEntity.getContact());
        accountEntity.setContactPhone(contactPhone);
        accountEntity.setDisabledFlag(false);
        accountEntity.setDeletedFlag(false);
        accountEntity.setCreateTime(LocalDateTime.now());
        accountEntity.setUpdateTime(LocalDateTime.now());
        bidPortalAccountDao.insert(accountEntity);

        return loginAndBuildVO(accountEntity);
    }

    /**
     * 供应商门户登录
     */
    public ResponseDTO<BidPortalAuthVO> login(BidPortalAuthLoginForm loginForm) {
        String loginName = normalizeLoginName(loginForm.getLoginName());
        BidPortalAccountEntity accountEntity = getByLoginName(loginName);
        if (accountEntity == null || !SecurityPasswordService.matchesPwd(loginForm.getPassword(), accountEntity.getLoginPwd())) {
            return ResponseDTO.userErrorParam("登录账号或密码错误");
        }
        if (Boolean.TRUE.equals(accountEntity.getDisabledFlag())) {
            return ResponseDTO.userErrorParam("门户账号已禁用");
        }
        return loginAndBuildVO(accountEntity);
    }

    /**
     * 查询当前门户登录态
     */
    public ResponseDTO<BidPortalAuthVO> getCurrent(BidPortalRequestUser requestUser) {
        if (requestUser == null) {
            return ResponseDTO.error(UserErrorCode.LOGIN_STATE_INVALID);
        }
        BidPortalAuthVO authVO = SmartBeanUtil.copy(requestUser, BidPortalAuthVO.class);
        return ResponseDTO.ok(authVO);
    }

    /**
     * 退出门户登录
     */
    public ResponseDTO<String> logout() {
        StpUtil.logout();
        return ResponseDTO.ok();
    }

    /**
     * 根据 sa-token loginId 获取门户请求用户
     */
    public BidPortalRequestUser getRequestUser(String loginId, HttpServletRequest request) {
        Long accountId = parseAccountId(loginId);
        if (accountId == null) {
            return null;
        }
        BidPortalAccountEntity accountEntity = bidPortalAccountDao.selectById(accountId);
        if (accountEntity == null || Boolean.TRUE.equals(accountEntity.getDeletedFlag()) || Boolean.TRUE.equals(accountEntity.getDisabledFlag())) {
            return null;
        }
        BidPortalRequestUser requestUser = SmartBeanUtil.copy(accountEntity, BidPortalRequestUser.class);
        requestUser.setIp(JakartaServletUtil.getClientIP(request));
        requestUser.setUserAgent(JakartaServletUtil.getHeaderIgnoreCase(request, RequestHeaderConst.USER_AGENT));
        return requestUser;
    }

    private ResponseDTO<BidPortalAuthVO> loginAndBuildVO(BidPortalAccountEntity accountEntity) {
        StpUtil.login(LOGIN_ID_PREFIX + accountEntity.getPortalAccountId(), "BID_PORTAL");
        String token = StpUtil.getTokenValue();

        accountEntity.setLastLoginTime(LocalDateTime.now());
        accountEntity.setUpdateTime(LocalDateTime.now());
        bidPortalAccountDao.updateById(accountEntity);

        BidPortalAuthVO authVO = SmartBeanUtil.copy(accountEntity, BidPortalAuthVO.class);
        authVO.setToken(token);
        return ResponseDTO.ok(authVO);
    }

    private Long parseAccountId(String loginId) {
        if (!StringUtils.startsWith(loginId, LOGIN_ID_PREFIX)) {
            return null;
        }
        try {
            return Long.parseLong(loginId.substring(LOGIN_ID_PREFIX.length()));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private BidPortalAccountEntity getByLoginName(String loginName) {
        return bidPortalAccountDao.selectOne(new LambdaQueryWrapper<BidPortalAccountEntity>()
                .eq(BidPortalAccountEntity::getLoginName, loginName)
                .eq(BidPortalAccountEntity::getDeletedFlag, false)
                .last("LIMIT 1"));
    }

    private boolean existsByLoginName(String loginName) {
        return getByLoginName(loginName) != null;
    }

    private boolean existsBySupplierCreditCode(String supplierCreditCode) {
        Long count = bidPortalAccountDao.selectCount(new LambdaQueryWrapper<BidPortalAccountEntity>()
                .eq(BidPortalAccountEntity::getSupplierCreditCode, supplierCreditCode)
                .eq(BidPortalAccountEntity::getDeletedFlag, false));
        return count != null && count > 0;
    }

    private String normalizeLoginName(String loginName) {
        return StringUtils.trim(loginName);
    }

    private String normalizeSupplierCreditCode(String supplierCreditCode) {
        return StringUtils.upperCase(StringUtils.trim(supplierCreditCode));
    }
}
