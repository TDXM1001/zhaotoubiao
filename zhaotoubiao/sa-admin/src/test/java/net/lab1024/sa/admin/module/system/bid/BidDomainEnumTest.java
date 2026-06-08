package net.lab1024.sa.admin.module.system.bid;

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
}
