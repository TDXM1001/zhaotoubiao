package net.lab1024.sa.admin.module.system.bid;

import net.lab1024.sa.admin.module.system.bid.common.constant.BidAwardStatusEnum;
import net.lab1024.sa.admin.module.system.bid.common.constant.BidEvaluationStatusEnum;
import net.lab1024.sa.admin.module.system.bid.common.constant.BidLotStatusEnum;
import net.lab1024.sa.admin.module.system.bid.common.constant.BidOpeningStatusEnum;
import net.lab1024.sa.admin.module.system.bid.common.constant.BidProjectStatusEnum;
import net.lab1024.sa.admin.module.system.bid.common.constant.BidSubmissionStatusEnum;
import net.lab1024.sa.admin.module.system.bid.common.constant.BidTenderVersionTypeEnum;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 招投标领域枚举合同测试。
 *
 * @author Codex
 * @date 2026-06-08
 */
class BidDomainEnumTest {

    @Test
    void 招标文件版本类型应与数据库字典值一致() {
        assertEquals(Arrays.asList("TENDER_MAIN", "ANNOUNCEMENT", "CLARIFICATION", "CORRECTION"),
                Arrays.stream(BidTenderVersionTypeEnum.values()).map(BidTenderVersionTypeEnum::getCode).toList());
        assertTrue(BidTenderVersionTypeEnum.contains("TENDER_MAIN"));
        assertTrue(BidTenderVersionTypeEnum.contains("CLARIFICATION"));
    }

    @Test
    void P2状态枚举应覆盖开评定标最小闭环() {
        assertTrue(BidProjectStatusEnum.contains("OPENING_IN_PROGRESS"));
        assertTrue(BidProjectStatusEnum.contains("EVALUATING"));
        assertTrue(BidProjectStatusEnum.contains("AWARDED"));

        assertTrue(BidLotStatusEnum.contains("OPENED"));
        assertTrue(BidLotStatusEnum.contains("EVALUATING"));
        assertTrue(BidLotStatusEnum.contains("AWARDED"));

        assertTrue(BidSubmissionStatusEnum.contains("OPENED"));

        assertEquals(Arrays.asList("PENDING", "IN_PROGRESS", "COMPLETED", "ABNORMAL_CLOSED"),
                Arrays.stream(BidOpeningStatusEnum.values()).map(BidOpeningStatusEnum::getCode).toList());
        assertEquals(Arrays.asList("PENDING", "SCORING", "SUMMARIZING", "FINALIZED", "ROLLED_BACK"),
                Arrays.stream(BidEvaluationStatusEnum.values()).map(BidEvaluationStatusEnum::getCode).toList());
        assertEquals(Arrays.asList("PENDING", "REVIEWING", "CONFIRMED", "ROLLED_BACK", "CANCELLED"),
                Arrays.stream(BidAwardStatusEnum.values()).map(BidAwardStatusEnum::getCode).toList());
    }
}
