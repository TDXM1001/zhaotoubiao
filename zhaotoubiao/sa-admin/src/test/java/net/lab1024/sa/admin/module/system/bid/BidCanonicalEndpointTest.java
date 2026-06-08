package net.lab1024.sa.admin.module.system.bid;

import cn.dev33.satoken.annotation.SaCheckPermission;
import net.lab1024.sa.admin.module.system.bid.lot.controller.BidLotController;
import net.lab1024.sa.admin.module.system.bid.lot.domain.form.BidLotActionForm;
import net.lab1024.sa.admin.module.system.bid.project.controller.BidProjectController;
import net.lab1024.sa.admin.module.system.bid.project.domain.form.BidProjectActionForm;
import net.lab1024.sa.admin.module.system.bid.registration.controller.BidRegistrationController;
import net.lab1024.sa.admin.module.system.bid.registration.domain.form.BidRegistrationActionForm;
import net.lab1024.sa.admin.module.system.bid.submission.controller.BidSubmissionController;
import net.lab1024.sa.admin.module.system.bid.submission.domain.form.BidSubmissionActionForm;
import net.lab1024.sa.admin.module.system.bid.tender.controller.BidTenderController;
import net.lab1024.sa.admin.module.system.bid.tender.domain.form.BidTenderActionForm;
import net.lab1024.sa.base.common.domain.ResponseDTO;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 招投标 canonical API 映射测试。
 *
 * <p>这里只校验 Controller 注解，不启动 Spring 上下文，避免数据库和鉴权环境影响接口契约测试。</p>
 */
class BidCanonicalEndpointTest {

    @Test
    void 项目接口应同时保留旧路径并提供资源化路径() {
        assertPost(BidProjectController.class, "/bid/project/queryPage", "bid:project:query");
        assertPost(BidProjectController.class, "/bid/projects/search", "bid:project:query");
        assertGet(BidProjectController.class, "/bid/project/get/{projectId}", "bid:project:query");
        assertGet(BidProjectController.class, "/bid/projects/{projectId}", "bid:project:query");
        assertGet(BidProjectController.class, "/bid/project/queryList", "bid:project:query");
        assertGet(BidProjectController.class, "/bid/projects/options", "bid:project:query");
        assertPost(BidProjectController.class, "/bid/project/add", "bid:project:create");
        assertPost(BidProjectController.class, "/bid/projects", "bid:project:create");
        assertPost(BidProjectController.class, "/bid/project/update", "bid:project:update");
        assertPut(BidProjectController.class, "/bid/projects/{projectId}", "bid:project:update");
        assertPost(BidProjectController.class, "/bid/project/submitPlan", "bid:project:submit-plan");
        assertPost(BidProjectController.class, "/bid/projects/{projectId}/actions/submit-plan", "bid:project:submit-plan");
        assertPost(BidProjectController.class, "/bid/project/publish", "bid:project:publish");
        assertPost(BidProjectController.class, "/bid/projects/{projectId}/actions/publish-project", "bid:project:publish");
        assertPost(BidProjectController.class, "/bid/project/cancel", "bid:project:cancel");
        assertPost(BidProjectController.class, "/bid/projects/{projectId}/actions/cancel-project", "bid:project:cancel");
    }

    @Test
    void 标段接口应同时保留旧路径并提供资源化路径() {
        assertPost(BidLotController.class, "/bid/lot/queryPage", "bid:lot:query");
        assertPost(BidLotController.class, "/bid/lots/search", "bid:lot:query");
        assertGet(BidLotController.class, "/bid/lot/get/{lotId}", "bid:lot:query");
        assertGet(BidLotController.class, "/bid/lots/{lotId}", "bid:lot:query");
        assertGet(BidLotController.class, "/bid/lot/queryByProjectId/{projectId}", "bid:lot:query");
        assertGet(BidLotController.class, "/bid/projects/{projectId}/lots", "bid:lot:query");
        assertPost(BidLotController.class, "/bid/lot/add", "bid:lot:create");
        assertPost(BidLotController.class, "/bid/lots", "bid:lot:create");
        assertPost(BidLotController.class, "/bid/lot/update", "bid:lot:update");
        assertPut(BidLotController.class, "/bid/lots/{lotId}", "bid:lot:update");
        assertPost(BidLotController.class, "/bid/lot/closeBid", "bid:lot:close-bid");
        assertPost(BidLotController.class, "/bid/lots/{lotId}/actions/close-bid", "bid:lot:close-bid");
        assertPost(BidLotController.class, "/bid/lot/void", "bid:lot:void");
        assertPost(BidLotController.class, "/bid/lots/{lotId}/actions/void-lot", "bid:lot:void");
    }

    @Test
    void 报名接口应同时保留旧路径并提供资源化路径() {
        assertPost(BidRegistrationController.class, "/bid/registration/queryPage", "bid:registration:query");
        assertPost(BidRegistrationController.class, "/bid/registrations/search", "bid:registration:query");
        assertGet(BidRegistrationController.class, "/bid/registration/get/{registrationId}", "bid:registration:query");
        assertGet(BidRegistrationController.class, "/bid/registrations/{registrationId}", "bid:registration:query");
        assertPost(BidRegistrationController.class, "/bid/registration/add", "bid:registration:create");
        assertPost(BidRegistrationController.class, "/bid/registrations", "bid:registration:create");
        assertPost(BidRegistrationController.class, "/bid/registration/approve", "bid:registration:approve");
        assertPost(BidRegistrationController.class, "/bid/registrations/{registrationId}/actions/approve-registration", "bid:registration:approve");
        assertPost(BidRegistrationController.class, "/bid/registration/reject", "bid:registration:reject");
        assertPost(BidRegistrationController.class, "/bid/registrations/{registrationId}/actions/reject-registration", "bid:registration:reject");
        assertPost(BidRegistrationController.class, "/bid/registration/cancel", "bid:registration:cancel");
        assertPost(BidRegistrationController.class, "/bid/registrations/{registrationId}/actions/cancel-registration", "bid:registration:cancel");
    }

    @Test
    void 招标文件接口应只提供资源化路径() {
        assertPost(BidTenderController.class, "/bid/tenders/search", "bid:tender:query");
        assertGet(BidTenderController.class, "/bid/tenders/{tenderVersionId}", "bid:tender:query");
        assertGet(BidTenderController.class, "/bid/lots/{lotId}/tenders/active", "bid:tender:query");
        assertPost(BidTenderController.class, "/bid/tenders", "bid:tender:create");
        assertPut(BidTenderController.class, "/bid/tenders/{tenderVersionId}", "bid:tender:update");
        assertPost(BidTenderController.class, "/bid/tenders/{tenderVersionId}/actions/publish-tender", "bid:tender:publish-tender");
        assertPost(BidTenderController.class, "/bid/tenders/{tenderVersionId}/actions/clarify-tender", "bid:tender:clarify-tender");
        assertPost(BidTenderController.class, "/bid/tenders/{tenderVersionId}/actions/withdraw-tender", "bid:tender:withdraw-tender");
        assertGet(BidTenderController.class, "/bid/tenders/{tenderVersionId}/attachments/{attachmentId}/download", "bid:tender:query");
        assertGet(BidTenderController.class, "/bid/tenders/{tenderVersionId}/attachments/{attachmentId}/preview-url", "bid:tender:query");
    }

    @Test
    void 投标提交接口应只提供资源化路径() {
        assertPost(BidSubmissionController.class, "/bid/submissions/search", "bid:submission:query");
        assertGet(BidSubmissionController.class, "/bid/submissions/{submissionId}", "bid:submission:query");
        assertGet(BidSubmissionController.class, "/bid/registrations/{registrationId}/submission", "bid:submission:query");
        assertPost(BidSubmissionController.class, "/bid/submissions", "bid:submission:create");
        assertPost(BidSubmissionController.class, "/bid/submissions/{submissionId}/actions/submit-bid", "bid:submission:submit-bid");
        assertPost(BidSubmissionController.class, "/bid/submissions/{submissionId}/actions/withdraw-bid", "bid:submission:withdraw-bid");
        assertGet(BidSubmissionController.class, "/bid/submissions/{submissionId}/attachments/{attachmentId}/download", "bid:submission:download");
        assertGet(BidSubmissionController.class, "/bid/submissions/{submissionId}/attachments/{attachmentId}/preview-url", "bid:submission:download");
    }

    @Test
    void 资源化动作接口应拒绝路径ID与表单ID不一致() {
        BidProjectActionForm projectActionForm = new BidProjectActionForm();
        projectActionForm.setProjectId(2L);
        projectActionForm.setVersion(1);
        ResponseDTO<String> projectResult = new BidProjectController().publishResource(1L, projectActionForm);
        assertFalse(projectResult.getOk(), "项目路径ID与表单ID不一致时应返回参数错误");

        BidLotActionForm lotActionForm = new BidLotActionForm();
        lotActionForm.setLotId(2L);
        lotActionForm.setVersion(1);
        ResponseDTO<String> lotResult = new BidLotController().closeBidResource(1L, lotActionForm);
        assertFalse(lotResult.getOk(), "标段路径ID与表单ID不一致时应返回参数错误");

        BidRegistrationActionForm registrationActionForm = new BidRegistrationActionForm();
        registrationActionForm.setRegistrationId(2L);
        registrationActionForm.setVersion(1);
        ResponseDTO<String> registrationResult = new BidRegistrationController().approveResource(1L, registrationActionForm);
        assertFalse(registrationResult.getOk(), "报名路径ID与表单ID不一致时应返回参数错误");

        BidTenderActionForm tenderActionForm = new BidTenderActionForm();
        tenderActionForm.setTenderVersionId(2L);
        tenderActionForm.setVersion(1);
        ResponseDTO<String> tenderResult = new BidTenderController().publishResource(1L, tenderActionForm);
        assertFalse(tenderResult.getOk(), "招标文件路径ID与表单ID不一致时应返回参数错误");

        BidSubmissionActionForm submissionActionForm = new BidSubmissionActionForm();
        submissionActionForm.setSubmissionId(2L);
        submissionActionForm.setVersion(1);
        ResponseDTO<String> submissionResult = new BidSubmissionController().withdrawResource(1L, submissionActionForm);
        assertFalse(submissionResult.getOk(), "投标路径ID与表单ID不一致时应返回参数错误");
    }

    private void assertGet(Class<?> controllerClass, String path, String permission) {
        assertEndpoint(controllerClass, path, permission, GetMapping.class);
    }

    private void assertPost(Class<?> controllerClass, String path, String permission) {
        assertEndpoint(controllerClass, path, permission, PostMapping.class);
    }

    private void assertPut(Class<?> controllerClass, String path, String permission) {
        assertEndpoint(controllerClass, path, permission, PutMapping.class);
    }

    private <T extends java.lang.annotation.Annotation> void assertEndpoint(Class<?> controllerClass, String path, String permission, Class<T> mappingClass) {
        Method method = findMethod(controllerClass, path, mappingClass)
                .orElseThrow(() -> new AssertionError("缺少接口映射：" + controllerClass.getSimpleName() + " " + path));
        SaCheckPermission permissionAnnotation = method.getAnnotation(SaCheckPermission.class);
        assertNotNull(permissionAnnotation, "缺少权限注解：" + controllerClass.getSimpleName() + " " + path);
        assertTrue(Arrays.asList(permissionAnnotation.value()).contains(permission),
                "权限码不匹配：" + controllerClass.getSimpleName() + " " + path);
    }

    private <T extends java.lang.annotation.Annotation> Optional<Method> findMethod(Class<?> controllerClass, String path, Class<T> mappingClass) {
        return Arrays.stream(controllerClass.getDeclaredMethods())
                .filter(method -> method.isAnnotationPresent(mappingClass))
                .filter(method -> containsPath(method, path, mappingClass))
                .findFirst();
    }

    private <T extends java.lang.annotation.Annotation> boolean containsPath(Method method, String path, Class<T> mappingClass) {
        T mapping = method.getAnnotation(mappingClass);
        if (mapping instanceof GetMapping getMapping) {
            return contains(getMapping.value(), path) || contains(getMapping.path(), path);
        }
        if (mapping instanceof PostMapping postMapping) {
            return contains(postMapping.value(), path) || contains(postMapping.path(), path);
        }
        if (mapping instanceof PutMapping putMapping) {
            return contains(putMapping.value(), path) || contains(putMapping.path(), path);
        }
        return false;
    }

    private boolean contains(String[] paths, String expectedPath) {
        return Arrays.asList(paths).contains(expectedPath);
    }
}
