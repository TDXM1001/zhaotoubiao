package net.lab1024.sa.admin.module.system.bid.common.constant;

/**
 * 招标文件状态枚举
 *
 * @author Codex
 * @date 2026-06-08
 */
public enum BidTenderStatusEnum {

    DRAFT("DRAFT"),
    ACTIVE("ACTIVE"),
    SUPERSEDED("SUPERSEDED"),
    WITHDRAWN("WITHDRAWN");

    private final String code;

    BidTenderStatusEnum(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
