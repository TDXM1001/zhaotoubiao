package net.lab1024.sa.admin.module.system.bid.common.constant;

import java.util.Arrays;

/**
 * 标段状态枚举
 *
 * @author Codex
 * @date 2026-06-07
 */
public enum BidLotStatusEnum {

    DRAFT("DRAFT", "草稿"),
    BIDDING("BIDDING", "投标中"),
    BID_CLOSED("BID_CLOSED", "已截标"),
    VOIDED("VOIDED", "已废止");

    private final String code;

    private final String description;

    BidLotStatusEnum(String code, String description) {
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
