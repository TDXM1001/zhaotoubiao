package net.lab1024.sa.admin.interceptor;

import cn.dev33.satoken.exception.SaTokenException;
import cn.dev33.satoken.stp.StpUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.admin.module.system.bid.portal.domain.vo.BidPortalRequestUser;
import net.lab1024.sa.admin.module.system.bid.portal.service.BidPortalAuthService;
import net.lab1024.sa.base.common.code.SystemErrorCode;
import net.lab1024.sa.base.common.code.UserErrorCode;
import net.lab1024.sa.base.common.domain.ResponseDTO;
import net.lab1024.sa.base.common.util.SmartRequestUtil;
import net.lab1024.sa.base.common.util.SmartResponseUtil;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 供应商门户拦截器
 *
 * @author Codex
 * @date 2026-06-08
 */
@Slf4j
@Component
public class BidPortalInterceptor implements HandlerInterceptor {

    @Resource
    private BidPortalAuthService bidPortalAuthService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (HttpMethod.OPTIONS.toString().equals(request.getMethod())) {
            response.setStatus(HttpStatus.NO_CONTENT.value());
            return false;
        }

        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        try {
            String tokenValue = StpUtil.getTokenValue();
            String loginId = (String) StpUtil.getLoginIdByToken(tokenValue);
            BidPortalRequestUser requestUser = bidPortalAuthService.getRequestUser(loginId, request);
            if (requestUser == null) {
                SmartResponseUtil.write(response, ResponseDTO.error(UserErrorCode.LOGIN_STATE_INVALID));
                return false;
            }

            StpUtil.updateLastActiveToNow();
            SmartRequestUtil.setRequestUser(requestUser);
            return true;
        } catch (SaTokenException e) {
            SmartResponseUtil.write(response, ResponseDTO.error(UserErrorCode.LOGIN_STATE_INVALID));
            return false;
        } catch (Throwable e) {
            SmartResponseUtil.write(response, ResponseDTO.error(SystemErrorCode.SYSTEM_ERROR));
            log.error(e.getMessage(), e);
            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        SmartRequestUtil.remove();
    }
}
