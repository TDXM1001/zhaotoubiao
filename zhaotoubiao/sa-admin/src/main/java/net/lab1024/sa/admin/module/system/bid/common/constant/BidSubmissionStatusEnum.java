package net.lab1024.sa.admin.module.system.bid.common.constant;

import java.util.Arrays;

/**
 * 投标提交状态枚举
 *
 * @author Codex
 * @date 2026-06-08
 */
public enum BidSubmissionStatusEnum {

    QUALIFIED("QUALIFIED", "已通过资格"),
    SUBMITTED("SUBMITTED", "已投标"),
    WITHDRAWN("WITHDRAWN", "已撤回"),
    OPENED("OPENED", "已开标");

    private final String code;

    private final String description;

    BidSubmissionStatusEnum(String code, String description) {
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
