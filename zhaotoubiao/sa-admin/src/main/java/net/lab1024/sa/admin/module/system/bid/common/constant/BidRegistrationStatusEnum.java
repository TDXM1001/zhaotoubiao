package net.lab1024.sa.admin.module.system.bid.common.constant;

/**
 * 供应商报名状态枚举
 *
 * @author Codex
 * @date 2026-06-07
 */
public enum BidRegistrationStatusEnum {

    SUBMITTED("SUBMITTED", "待审核"),
    QUALIFIED("QUALIFIED", "已通过"),
    REJECTED("REJECTED", "已驳回"),
    CANCELLED("CANCELLED", "已取消");

    private final String code;

    private final String name;

    BidRegistrationStatusEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }
}
