package net.lab1024.sa.admin.module.system.bid.common.constant;

import java.util.Arrays;

/**
 * 开标状态枚举
 *
 * @author Codex
 * @date 2026-06-08
 */
public enum BidOpeningStatusEnum {

    PENDING("PENDING", "待开标"),
    IN_PROGRESS("IN_PROGRESS", "开标中"),
    COMPLETED("COMPLETED", "已完成"),
    ABNORMAL_CLOSED("ABNORMAL_CLOSED", "异常关闭");

    private final String code;

    private final String description;

    BidOpeningStatusEnum(String code, String description) {
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
