package net.lab1024.sa.admin.module.system.bid.common.constant;

/**
 * 招投标业务类型枚举
 *
 * @author Codex
 * @date 2026-06-07
 */
public enum BidBusinessTypeEnum {

    PROJECT("PROJECT"),
    LOT("LOT"),
    REGISTRATION("REGISTRATION");

    private final String code;

    BidBusinessTypeEnum(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
