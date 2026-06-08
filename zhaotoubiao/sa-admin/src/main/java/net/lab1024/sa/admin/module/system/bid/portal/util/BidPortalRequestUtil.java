package net.lab1024.sa.admin.module.system.bid.portal.util;

import net.lab1024.sa.admin.module.system.bid.portal.domain.vo.BidPortalRequestUser;
import net.lab1024.sa.base.common.domain.RequestUser;
import net.lab1024.sa.base.common.util.SmartRequestUtil;

/**
 * 供应商门户请求上下文工具
 *
 * @author Codex
 * @date 2026-06-08
 */
public final class BidPortalRequestUtil {

    private BidPortalRequestUtil() {
    }

    public static BidPortalRequestUser getRequestUser() {
        RequestUser requestUser = SmartRequestUtil.getRequestUser();
        if (requestUser instanceof BidPortalRequestUser bidPortalRequestUser) {
            return bidPortalRequestUser;
        }
        return null;
    }
}
