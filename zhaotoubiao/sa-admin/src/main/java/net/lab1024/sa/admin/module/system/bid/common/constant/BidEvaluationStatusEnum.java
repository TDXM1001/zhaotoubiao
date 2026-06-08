package net.lab1024.sa.admin.module.system.bid.common.constant;

import java.util.Arrays;

/**
 * 评标状态枚举
 *
 * @author Codex
 * @date 2026-06-08
 */
public enum BidEvaluationStatusEnum {

    PENDING("PENDING", "待评标"),
    SCORING("SCORING", "评分中"),
    SUMMARIZING("SUMMARIZING", "汇总中"),
    FINALIZED("FINALIZED", "已定稿"),
    ROLLED_BACK("ROLLED_BACK", "已回退");

    private final String code;

    private final String description;

    BidEvaluationStatusEnum(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static boolean contains(String code) {
        return Arrays.stream(values()).anyMatch(item -> item.code.equals(code));
    }
}
