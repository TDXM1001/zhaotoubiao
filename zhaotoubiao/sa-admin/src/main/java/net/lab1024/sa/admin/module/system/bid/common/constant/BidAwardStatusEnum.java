package net.lab1024.sa.admin.module.system.bid.common.constant;

import java.util.Arrays;

/**
 * 定标状态枚举
 *
 * @author Codex
 * @date 2026-06-08
 */
public enum BidAwardStatusEnum {

    PENDING("PENDING", "待定标"),
    REVIEWING("REVIEWING", "确认中"),
    CONFIRMED("CONFIRMED", "已确认"),
    ROLLED_BACK("ROLLED_BACK", "已回退"),
    CANCELLED("CANCELLED", "已取消");

    private final String code;

    private final String description;

    BidAwardStatusEnum(String code, String description) {
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
