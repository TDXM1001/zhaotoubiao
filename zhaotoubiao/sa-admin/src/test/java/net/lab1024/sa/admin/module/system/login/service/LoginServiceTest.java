package net.lab1024.sa.admin.module.system.login.service;

import net.lab1024.sa.base.common.enumeration.UserTypeEnum;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class LoginServiceTest {

    @Test
    void 后台登录解析只接受员工用户前缀() {
        LoginService loginService = new LoginService();

        assertEquals(9L, loginService.getEmployeeIdByLoginId(UserTypeEnum.ADMIN_EMPLOYEE.getValue() + ":9"));
        assertNull(loginService.getEmployeeIdByLoginId("2:9"));
        assertNull(loginService.getEmployeeIdByLoginId("BID_PORTAL_SUPPLIER:9"));
    }
}
