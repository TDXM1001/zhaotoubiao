package net.lab1024.sa.admin.module.system.bid.common.constant;

import java.util.Arrays;

/**
 * 招标项目状态枚举
 *
 * @author Codex
 * @date 2026-06-07
 */
public enum BidProjectStatusEnum {

    DRAFT("DRAFT", "草稿"),
    PLANNED("PLANNED", "已提交计划"),
    PUBLISHED("PUBLISHED", "已发布"),
    OPENING_IN_PROGRESS("OPENING_IN_PROGRESS", "开标中"),
    EVALUATING("EVALUATING", "评标中"),
    AWARDED("AWARDED", "已定标"),
    CANCELLED("CANCELLED", "已作废"),
    ARCHIVED("ARCHIVED", "已归档");

    private final String code;

    private final String description;

    BidProjectStatusEnum(String code, String description) {
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
