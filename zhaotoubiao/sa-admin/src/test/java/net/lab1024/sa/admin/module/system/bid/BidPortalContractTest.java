package net.lab1024.sa.admin.module.system.bid;

import cn.dev33.satoken.annotation.SaCheckPermission;
import jakarta.validation.constraints.NotBlank;
import net.lab1024.sa.admin.module.system.bid.portal.controller.BidPortalController;
import net.lab1024.sa.admin.module.system.bid.portal.domain.form.BidPortalRegistrationCreateForm;
import net.lab1024.sa.admin.module.system.bid.portal.domain.form.BidPortalSubmissionActionForm;
import net.lab1024.sa.admin.module.system.bid.portal.domain.form.BidPortalSubmissionCreateForm;
import net.lab1024.sa.admin.module.system.bid.portal.domain.vo.BidPortalProjectVO;
import net.lab1024.sa.admin.module.system.bid.portal.domain.vo.BidPortalRegistrationVO;
import net.lab1024.sa.admin.module.system.bid.portal.domain.vo.BidPortalResultVO;
import net.lab1024.sa.admin.module.system.bid.portal.domain.vo.BidPortalSubmissionVO;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 供应商门户接口边界合同测试。
 *
 * @author Codex
 * @date 2026-06-08
 */
class BidPortalContractTest {

    @Test
    void 门户必须提供独立登录态接口() {
        assertEndpoint("/bid/portal/auth/register", PostMapping.class);
        assertEndpoint("/bid/portal/auth/login", PostMapping.class);
        assertEndpoint("/bid/portal/auth/logout", PostMapping.class);
        assertEndpoint("/bid/portal/auth/me", GetMapping.class);
    }

    @Test
    void 门户接口必须落在独立路径下且不复用内部权限码() {
        assertEndpoint("/bid/portal/projects/search", PostMapping.class);
        assertEndpoint("/bid/portal/projects/{projectId}", GetMapping.class);
        assertEndpoint("/bid/portal/registrations", PostMapping.class);
        assertEndpoint("/bid/portal/registrations/{registrationId}", GetMapping.class);
        assertEndpoint("/bid/portal/submissions", PostMapping.class);
        assertEndpoint("/bid/portal/submissions/{submissionId}", GetMapping.class);
        assertEndpoint("/bid/portal/submissions/{submissionId}/actions/submit-bid", PostMapping.class);
        assertEndpoint("/bid/portal/submissions/{submissionId}/actions/withdraw-bid", PostMapping.class);
        assertEndpoint("/bid/portal/lots/{lotId}/result", GetMapping.class);
    }

    @Test
    void 门户响应对象不得暴露内部动作字段() {
        assertNoAllowedActions(BidPortalProjectVO.class);
        assertNoAllowedActions(BidPortalRegistrationVO.class);
        assertNoAllowedActions(BidPortalSubmissionVO.class);
        assertNoAllowedActions(BidPortalResultVO.class);
    }

    @Test
    void 门户业务表单不得强制客户端提交供应商身份字段() throws NoSuchFieldException {
        assertNoNotBlank(BidPortalRegistrationCreateForm.class, "supplierNameSnapshot");
        assertNoNotBlank(BidPortalRegistrationCreateForm.class, "supplierCreditCode");
        assertNoNotBlank(BidPortalSubmissionCreateForm.class, "supplierCreditCode");
        assertNoNotBlank(BidPortalSubmissionActionForm.class, "supplierCreditCode");
    }

    private void assertNoAllowedActions(Class<?> voClass) {
        assertFalse(Arrays.stream(voClass.getDeclaredFields())
                .anyMatch(field -> "allowedActions".equals(field.getName())));
    }

    private void assertNoNotBlank(Class<?> formClass, String fieldName) throws NoSuchFieldException {
        assertFalse(formClass.getDeclaredField(fieldName).isAnnotationPresent(NotBlank.class),
                "门户供应商身份应从登录态获取，不应强制客户端提交：" + formClass.getSimpleName() + "." + fieldName);
    }

    private <T extends Annotation> void assertEndpoint(String path, Class<T> mappingClass) {
        Method method = findMethod(path, mappingClass)
                .orElseThrow(() -> new AssertionError("缺少门户接口：" + path));
        assertFalse(method.isAnnotationPresent(SaCheckPermission.class), "门户接口不应复用内部系统权限码：" + path);
    }

    private <T extends Annotation> Optional<Method> findMethod(String path, Class<T> mappingClass) {
        return Arrays.stream(BidPortalController.class.getDeclaredMethods())
                .filter(method -> method.isAnnotationPresent(mappingClass))
                .filter(method -> containsPath(method, path, mappingClass))
                .findFirst();
    }

    private <T extends Annotation> boolean containsPath(Method method, String path, Class<T> mappingClass) {
        T mapping = method.getAnnotation(mappingClass);
        if (mapping instanceof GetMapping getMapping) {
            return Arrays.asList(getMapping.value()).contains(path) || Arrays.asList(getMapping.path()).contains(path);
        }
        if (mapping instanceof PostMapping postMapping) {
            return Arrays.asList(postMapping.value()).contains(path) || Arrays.asList(postMapping.path()).contains(path);
        }
        return false;
    }
}
