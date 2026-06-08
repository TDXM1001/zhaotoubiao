package net.lab1024.sa.admin.module.system.bid.common.constant;

import java.util.Arrays;

/**
 * 招标文件版本类型枚举。
 *
 * @author Codex
 * @date 2026-06-08
 */
public enum BidTenderVersionTypeEnum {

    TENDER_MAIN("TENDER_MAIN", "招标文件"),
    ANNOUNCEMENT("ANNOUNCEMENT", "公告"),
    CLARIFICATION("CLARIFICATION", "澄清"),
    CORRECTION("CORRECTION", "更正");

    private final String code;

    private final String description;

    BidTenderVersionTypeEnum(String code, String description) {
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
