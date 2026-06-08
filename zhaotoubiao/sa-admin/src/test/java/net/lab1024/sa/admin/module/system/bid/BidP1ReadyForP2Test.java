package net.lab1024.sa.admin.module.system.bid;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import net.lab1024.sa.admin.module.system.bid.attachment.dao.BidAttachmentDao;
import net.lab1024.sa.admin.module.system.bid.attachment.domain.entity.BidAttachmentEntity;
import net.lab1024.sa.admin.module.system.bid.attachment.domain.form.BidAttachmentCreateForm;
import net.lab1024.sa.admin.module.system.bid.attachment.domain.vo.BidAttachmentVO;
import net.lab1024.sa.admin.module.system.bid.attachment.service.BidAttachmentService;
import net.lab1024.sa.admin.module.system.bid.common.constant.BidBusinessTypeEnum;
import net.lab1024.sa.admin.module.system.bid.common.constant.BidLotStatusEnum;
import net.lab1024.sa.admin.module.system.bid.common.constant.BidRegistrationStatusEnum;
import net.lab1024.sa.admin.module.system.bid.common.constant.BidSubmissionStatusEnum;
import net.lab1024.sa.admin.module.system.bid.common.constant.BidTenderStatusEnum;
import net.lab1024.sa.admin.module.system.bid.common.constant.BidTenderVersionTypeEnum;
import net.lab1024.sa.admin.module.system.bid.common.service.BidWorkflowHistoryService;
import net.lab1024.sa.admin.module.system.bid.lot.dao.BidLotDao;
import net.lab1024.sa.admin.module.system.bid.lot.domain.entity.BidLotEntity;
import net.lab1024.sa.admin.module.system.bid.project.dao.BidProjectDao;
import net.lab1024.sa.admin.module.system.bid.project.domain.entity.BidProjectEntity;
import net.lab1024.sa.admin.module.system.bid.registration.dao.BidRegistrationDao;
import net.lab1024.sa.admin.module.system.bid.registration.domain.entity.BidRegistrationEntity;
import net.lab1024.sa.admin.module.system.bid.submission.dao.BidSubmissionDao;
import net.lab1024.sa.admin.module.system.bid.submission.domain.entity.BidSubmissionEntity;
import net.lab1024.sa.admin.module.system.bid.submission.domain.entity.BidSubmissionVersionEntity;
import net.lab1024.sa.admin.module.system.bid.submission.domain.form.BidSubmissionActionForm;
import net.lab1024.sa.admin.module.system.bid.submission.domain.form.BidSubmissionCreateForm;
import net.lab1024.sa.admin.module.system.bid.submission.domain.form.BidSubmissionSubmitForm;
import net.lab1024.sa.admin.module.system.bid.submission.service.BidSubmissionService;
import net.lab1024.sa.admin.module.system.bid.tender.dao.BidTenderDao;
import net.lab1024.sa.admin.module.system.bid.tender.domain.entity.BidTenderVersionEntity;
import net.lab1024.sa.admin.module.system.bid.tender.domain.form.BidTenderActionForm;
import net.lab1024.sa.admin.module.system.bid.tender.domain.form.BidTenderCreateForm;
import net.lab1024.sa.admin.module.system.bid.tender.service.BidTenderService;
import net.lab1024.sa.base.common.domain.ResponseDTO;
import net.lab1024.sa.base.module.support.file.dao.FileDao;
import net.lab1024.sa.base.module.support.file.domain.entity.FileEntity;
import net.lab1024.sa.base.module.support.file.service.FileService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * P1 ready-for-P2 regression tests for admin bid backend real service flows.
 */
class BidP1ReadyForP2Test {

    @Test
    void publishAndClarifyKeepOnlyOneActiveTenderForLotAndKeepParentVersion() {
        TenderHarness harness = new TenderHarness();

        Long firstTenderId = harness.createDraftTender();
        ResponseDTO<String> firstPublish = harness.service.publish(tenderAction(firstTenderId, 0, "publish first"));
        assertTrue(firstPublish.getOk());

        Long secondTenderId = harness.createDraftTender();
        ResponseDTO<String> secondPublish = harness.service.publish(tenderAction(secondTenderId, 0, "publish second"));
        assertTrue(secondPublish.getOk());

        assertEquals(1, harness.activeCurrentTenderCount());
        assertEquals(BidTenderStatusEnum.SUPERSEDED.getCode(), harness.tenders.get(firstTenderId).getStatus());
        assertEquals(secondTenderId, harness.onlyActiveCurrentTender().getTenderVersionId());

        Integer activeVersion = harness.tenders.get(secondTenderId).getVersion();
        ResponseDTO<String> clarifyResult = harness.service.clarify(tenderAction(secondTenderId, activeVersion, "clarify"));
        assertTrue(clarifyResult.getOk());

        assertEquals(1, harness.activeCurrentTenderCount());
        BidTenderVersionEntity clarification = harness.onlyActiveCurrentTender();
        assertEquals(BidTenderVersionTypeEnum.CLARIFICATION.getCode(), clarification.getVersionType());
        assertEquals(secondTenderId, clarification.getParentVersionId());
        assertEquals(3, clarification.getVersionNo());
        assertEquals(BidTenderStatusEnum.SUPERSEDED.getCode(), harness.tenders.get(secondTenderId).getStatus());
    }

    @Test
    void repeatedSubmissionCreatesIncrementingTraceableVersionsAndWithdrawKeepsHistory() {
        SubmissionHarness harness = new SubmissionHarness();

        ResponseDTO<String> createResult = harness.service.create(submissionCreateForm());
        assertTrue(createResult.getOk());
        Long submissionId = Long.valueOf(createResult.getData());

        ResponseDTO<String> firstSubmit = harness.service.submitBid(submissionSubmitForm(submissionId, 0, "100.00"));
        assertTrue(firstSubmit.getOk());

        ResponseDTO<String> secondSubmit = harness.service.submitBid(submissionSubmitForm(submissionId, 1, "120.00"));
        assertTrue(secondSubmit.getOk());

        List<BidSubmissionVersionEntity> versions = harness.versionsFor(submissionId);
        assertEquals(2, versions.size());
        assertEquals(List.of(1, 2), versions.stream().map(BidSubmissionVersionEntity::getVersionNo).toList());
        assertEquals(1, versions.stream().filter(BidSubmissionVersionEntity::getEffective).count(),
                "Only the latest submitted version should remain effective for opening freeze.");
        assertTrue(versions.stream()
                        .filter(version -> Objects.equals(2, version.getVersionNo()))
                        .allMatch(BidSubmissionVersionEntity::getEffective),
                "The latest submitted version should be the effective one.");
        assertEquals(2, harness.submissions.get(submissionId).getLatestVersionNo());
        assertEquals(BidSubmissionStatusEnum.SUBMITTED.getCode(), harness.submissions.get(submissionId).getStatus());

        ResponseDTO<String> withdrawResult = harness.service.withdraw(submissionActionForm(submissionId, 2, "withdraw"));
        assertTrue(withdrawResult.getOk());

        assertEquals(BidSubmissionStatusEnum.WITHDRAWN.getCode(), harness.submissions.get(submissionId).getStatus());
        assertEquals(2, harness.versionsFor(submissionId).size());
        assertEquals(1, harness.versionsFor(submissionId).stream().filter(BidSubmissionVersionEntity::getEffective).count(),
                "Withdrawal must keep historical rows traceable without re-enabling old effective versions.");
    }

    @Test
    void attachmentDownloadPreviewAndAssociationAreScopedByBusinessOwnership() {
        BidAttachmentDao attachmentDao = mock(BidAttachmentDao.class);
        FileDao fileDao = mock(FileDao.class);
        FileService fileService = mock(FileService.class);
        BidAttachmentService attachmentService = new BidAttachmentService();
        inject(attachmentService, "bidAttachmentDao", attachmentDao);
        inject(attachmentService, "fileDao", fileDao);
        inject(attachmentService, "fileService", fileService);

        BidAttachmentVO tenderAttachment = attachment(10L, BidBusinessTypeEnum.TENDER_VERSION.getCode(), 100L, "tender-key");
        when(attachmentDao.getByBusinessAndAttachment(BidBusinessTypeEnum.TENDER_VERSION.getCode(), 100L, 10L))
                .thenReturn(tenderAttachment);
        when(attachmentDao.getByBusinessAndAttachment(BidBusinessTypeEnum.SUBMISSION_VERSION.getCode(), 200L, 10L))
                .thenReturn(null);

        ResponseDTO<String> wrongPreview = attachmentService.getPreviewUrl(BidBusinessTypeEnum.SUBMISSION_VERSION.getCode(), 200L, 10L);
        assertFalse(wrongPreview.getOk());
        verify(fileService, never()).getFileUrl(any());

        when(fileService.getFileUrl("tender-key")).thenReturn(ResponseDTO.ok("https://files.local/tender-key"));
        ResponseDTO<String> rightPreview = attachmentService.getPreviewUrl(BidBusinessTypeEnum.TENDER_VERSION.getCode(), 100L, 10L);
        assertTrue(rightPreview.getOk());
        assertEquals("https://files.local/tender-key", rightPreview.getData());

        FileEntity file = new FileEntity();
        file.setFileId(500L);
        when(fileDao.selectById(500L)).thenReturn(file);
        when(attachmentDao.countByBusinessAndFile(BidBusinessTypeEnum.TENDER_VERSION.getCode(), 100L, 500L))
                .thenReturn(0L);

        BidAttachmentCreateForm createForm = new BidAttachmentCreateForm();
        createForm.setFileId(500L);
        createForm.setFileCategory("TENDER_DOC");
        createForm.setVersionNo(3);

        ResponseDTO<String> createResult = attachmentService.create(BidBusinessTypeEnum.TENDER_VERSION.getCode(), 100L, 1L, 2L, createForm);
        assertTrue(createResult.getOk());
        verify(attachmentDao).insert(ArgumentMatchers.<BidAttachmentEntity>argThat(entity ->
                Objects.equals(BidBusinessTypeEnum.TENDER_VERSION.getCode(), entity.getBusinessType())
                        && Objects.equals(100L, entity.getBusinessId())
                        && Objects.equals(1L, entity.getProjectId())
                        && Objects.equals(2L, entity.getLotId())
                        && Objects.equals(3, entity.getVersionNo())
                        && Objects.equals(500L, entity.getFileId())));
    }

    private static BidTenderActionForm tenderAction(Long tenderVersionId, Integer version, String remark) {
        BidTenderActionForm form = new BidTenderActionForm();
        form.setTenderVersionId(tenderVersionId);
        form.setVersion(version);
        form.setRemark(remark);
        return form;
    }

    private static BidSubmissionCreateForm submissionCreateForm() {
        BidSubmissionCreateForm form = new BidSubmissionCreateForm();
        form.setProjectId(1L);
        form.setLotId(2L);
        form.setRegistrationId(3L);
        form.setRemark("create submission");
        return form;
    }

    private static BidSubmissionSubmitForm submissionSubmitForm(Long submissionId, Integer version, String amount) {
        BidSubmissionSubmitForm form = new BidSubmissionSubmitForm();
        form.setSubmissionId(submissionId);
        form.setVersion(version);
        form.setPriceAmount(new BigDecimal(amount));
        form.setContactName("contact");
        form.setContactPhone("13800000000");
        form.setFileManifestJson("[{\"name\":\"bid.pdf\"}]");
        form.setRemark("submit " + amount);
        return form;
    }

    private static BidSubmissionActionForm submissionActionForm(Long submissionId, Integer version, String remark) {
        BidSubmissionActionForm form = new BidSubmissionActionForm();
        form.setSubmissionId(submissionId);
        form.setVersion(version);
        form.setRemark(remark);
        return form;
    }

    private static BidAttachmentVO attachment(Long attachmentId, String businessType, Long businessId, String fileKey) {
        BidAttachmentVO vo = new BidAttachmentVO();
        vo.setAttachmentId(attachmentId);
        vo.setBusinessType(businessType);
        vo.setBusinessId(businessId);
        vo.setFileId(500L);
        vo.setFileKey(fileKey);
        return vo;
    }

    private static BidProjectEntity project() {
        BidProjectEntity project = new BidProjectEntity();
        project.setProjectId(1L);
        project.setDeletedFlag(Boolean.FALSE);
        project.setVersion(0);
        return project;
    }

    private static BidLotEntity biddingLot() {
        BidLotEntity lot = new BidLotEntity();
        lot.setProjectId(1L);
        lot.setLotId(2L);
        lot.setStatus(BidLotStatusEnum.BIDDING.getCode());
        lot.setBidStartTime(LocalDateTime.now().minusDays(1));
        lot.setBidEndTime(LocalDateTime.now().plusDays(1));
        lot.setDeletedFlag(Boolean.FALSE);
        lot.setVersion(0);
        return lot;
    }

    private static void inject(Object target, String fieldName, Object dependency) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, dependency);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

    private static final class TenderHarness {
        private final Map<Long, BidTenderVersionEntity> tenders = new LinkedHashMap<>();
        private final AtomicLong idGenerator = new AtomicLong(100);
        private final BidTenderService service = new BidTenderService();

        private TenderHarness() {
            BidTenderDao tenderDao = mock(BidTenderDao.class);
            BidProjectDao projectDao = mock(BidProjectDao.class);
            BidLotDao lotDao = mock(BidLotDao.class);

            when(projectDao.selectById(1L)).thenReturn(project());
            when(lotDao.selectById(2L)).thenReturn(biddingLot());
            when(tenderDao.insert(any(BidTenderVersionEntity.class))).thenAnswer(invocation -> {
                BidTenderVersionEntity entity = invocation.getArgument(0);
                entity.setTenderVersionId(idGenerator.incrementAndGet());
                tenders.put(entity.getTenderVersionId(), entity);
                return 1;
            });
            when(tenderDao.selectMaxVersionNoByLotId(2L)).thenAnswer(invocation ->
                    tenders.values().stream()
                            .filter(entity -> Objects.equals(2L, entity.getLotId()))
                            .filter(entity -> !Boolean.TRUE.equals(entity.getDeletedFlag()))
                            .map(BidTenderVersionEntity::getVersionNo)
                            .filter(Objects::nonNull)
                            .max(Integer::compareTo)
                            .orElse(null));
            when(tenderDao.selectById(anyLong())).thenAnswer(invocation -> tenders.get(invocation.getArgument(0)));
            when(tenderDao.supersedeCurrentByLotId(eq(2L), any(), any())).thenAnswer(invocation -> {
                Long excludedId = invocation.getArgument(1);
                long count = tenders.values().stream()
                        .filter(entity -> Objects.equals(2L, entity.getLotId()))
                        .filter(entity -> !Objects.equals(entity.getTenderVersionId(), excludedId))
                        .filter(entity -> Objects.equals(BidTenderStatusEnum.ACTIVE.getCode(), entity.getStatus()))
                        .filter(entity -> Boolean.TRUE.equals(entity.getCurrentFlag()))
                        .peek(entity -> {
                            entity.setStatus(BidTenderStatusEnum.SUPERSEDED.getCode());
                            entity.setCurrentFlag(null);
                            entity.setVersion(entity.getVersion() + 1);
                        })
                        .count();
                return (int) count;
            });
            when(tenderDao.updateWithVersion(any(BidTenderVersionEntity.class))).thenAnswer(invocation -> {
                BidTenderVersionEntity update = invocation.getArgument(0);
                BidTenderVersionEntity current = tenders.get(update.getTenderVersionId());
                if (current == null || !Objects.equals(current.getVersion(), update.getVersion())) {
                    return 0;
                }
                if (update.getStatus() != null) {
                    current.setStatus(update.getStatus());
                }
                if (update.getCurrentFlag() != null) {
                    current.setCurrentFlag(update.getCurrentFlag());
                }
                if (Objects.equals(BidTenderStatusEnum.SUPERSEDED.getCode(), update.getStatus())
                        || Objects.equals(BidTenderStatusEnum.WITHDRAWN.getCode(), update.getStatus())) {
                    current.setCurrentFlag(null);
                }
                if (update.getEffectiveTime() != null) {
                    current.setEffectiveTime(update.getEffectiveTime());
                }
                if (update.getPublishTime() != null) {
                    current.setPublishTime(update.getPublishTime());
                }
                current.setVersion(current.getVersion() + 1);
                return 1;
            });

            inject(service, "bidTenderDao", tenderDao);
            inject(service, "bidProjectDao", projectDao);
            inject(service, "bidLotDao", lotDao);
            inject(service, "bidWorkflowHistoryService", mock(BidWorkflowHistoryService.class));
            inject(service, "bidAttachmentService", mock(BidAttachmentService.class));
        }

        private Long createDraftTender() {
            BidTenderCreateForm form = new BidTenderCreateForm();
            form.setProjectId(1L);
            form.setLotId(2L);
            form.setVersionType(BidTenderVersionTypeEnum.TENDER_MAIN.getCode());
            form.setSummary("main tender");
            ResponseDTO<String> result = service.create(form);
            assertTrue(result.getOk());
            return tenders.values().stream()
                    .max(Comparator.comparing(BidTenderVersionEntity::getTenderVersionId))
                    .orElseThrow()
                    .getTenderVersionId();
        }

        private long activeCurrentTenderCount() {
            return tenders.values().stream()
                    .filter(entity -> Objects.equals(BidTenderStatusEnum.ACTIVE.getCode(), entity.getStatus()))
                    .filter(entity -> Boolean.TRUE.equals(entity.getCurrentFlag()))
                    .count();
        }

        private BidTenderVersionEntity onlyActiveCurrentTender() {
            List<BidTenderVersionEntity> active = tenders.values().stream()
                    .filter(entity -> Objects.equals(BidTenderStatusEnum.ACTIVE.getCode(), entity.getStatus()))
                    .filter(entity -> Boolean.TRUE.equals(entity.getCurrentFlag()))
                    .toList();
            assertEquals(1, active.size());
            return active.get(0);
        }
    }

    private static final class SubmissionHarness {
        private final Map<Long, BidSubmissionEntity> submissions = new LinkedHashMap<>();
        private final List<BidSubmissionVersionEntity> versions = new ArrayList<>();
        private final AtomicLong submissionIdGenerator = new AtomicLong(200);
        private final AtomicLong versionIdGenerator = new AtomicLong(300);
        private final BidSubmissionService service = new BidSubmissionService();

        private SubmissionHarness() {
            BidSubmissionDao submissionDao = mock(BidSubmissionDao.class);
            BidProjectDao projectDao = mock(BidProjectDao.class);
            BidLotDao lotDao = mock(BidLotDao.class);
            BidRegistrationDao registrationDao = mock(BidRegistrationDao.class);

            BidRegistrationEntity registration = new BidRegistrationEntity();
            registration.setRegistrationId(3L);
            registration.setProjectId(1L);
            registration.setLotId(2L);
            registration.setSupplierEnterpriseId(4L);
            registration.setSupplierNameSnapshot("supplier");
            registration.setSupplierCreditCode("913300000000000000");
            registration.setStatus(BidRegistrationStatusEnum.QUALIFIED.getCode());
            registration.setDeletedFlag(Boolean.FALSE);

            when(registrationDao.selectById(3L)).thenReturn(registration);
            when(projectDao.selectById(1L)).thenReturn(project());
            when(lotDao.selectById(2L)).thenReturn(biddingLot());
            when(submissionDao.selectCount(any(Wrapper.class))).thenReturn(0L);
            when(submissionDao.insert(any(BidSubmissionEntity.class))).thenAnswer(invocation -> {
                BidSubmissionEntity entity = invocation.getArgument(0);
                entity.setSubmissionId(submissionIdGenerator.incrementAndGet());
                submissions.put(entity.getSubmissionId(), entity);
                return 1;
            });
            when(submissionDao.selectById(anyLong())).thenAnswer(invocation -> submissions.get(invocation.getArgument(0)));
            when(submissionDao.updateWithVersion(any(BidSubmissionEntity.class))).thenAnswer(invocation -> {
                BidSubmissionEntity update = invocation.getArgument(0);
                BidSubmissionEntity current = submissions.get(update.getSubmissionId());
                if (current == null || !Objects.equals(current.getVersion(), update.getVersion())) {
                    return 0;
                }
                if (update.getStatus() != null) {
                    current.setStatus(update.getStatus());
                }
                if (update.getLatestVersionNo() != null) {
                    current.setLatestVersionNo(update.getLatestVersionNo());
                }
                if (update.getLatestSubmitTime() != null) {
                    current.setLatestSubmitTime(update.getLatestSubmitTime());
                }
                if (update.getReceiptNo() != null) {
                    current.setReceiptNo(update.getReceiptNo());
                }
                if (update.getWithdrawTime() != null) {
                    current.setWithdrawTime(update.getWithdrawTime());
                }
                if (update.getWithdrawReason() != null) {
                    current.setWithdrawReason(update.getWithdrawReason());
                }
                current.setVersion(current.getVersion() + 1);
                return 1;
            });
            when(submissionDao.disableEffectiveVersions(anyLong())).thenAnswer(invocation -> {
                Long submissionId = invocation.getArgument(0);
                long count = versions.stream()
                        .filter(version -> Objects.equals(submissionId, version.getSubmissionId()))
                        .filter(version -> Boolean.TRUE.equals(version.getEffective()))
                        .peek(version -> version.setEffective(Boolean.FALSE))
                        .count();
                return (int) count;
            });
            when(submissionDao.insertSubmissionVersion(any(BidSubmissionVersionEntity.class))).thenAnswer(invocation -> {
                BidSubmissionVersionEntity entity = invocation.getArgument(0);
                entity.setSubmissionVersionId(versionIdGenerator.incrementAndGet());
                versions.add(entity);
                return 1;
            });

            inject(service, "bidSubmissionDao", submissionDao);
            inject(service, "bidProjectDao", projectDao);
            inject(service, "bidLotDao", lotDao);
            inject(service, "bidRegistrationDao", registrationDao);
            inject(service, "bidWorkflowHistoryService", mock(BidWorkflowHistoryService.class));
            inject(service, "bidAttachmentService", mock(BidAttachmentService.class));
        }

        private List<BidSubmissionVersionEntity> versionsFor(Long submissionId) {
            return versions.stream()
                    .filter(version -> Objects.equals(submissionId, version.getSubmissionId()))
                    .toList();
        }
    }
}
