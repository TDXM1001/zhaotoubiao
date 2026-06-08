package net.lab1024.sa.base.common.util;

import net.lab1024.sa.base.common.constant.StringConst;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SmartIpUtilTest {

    @BeforeAll
    static void initSearcher() throws URISyntaxException {
        SmartIpUtil.init(Path.of(SmartIpUtilTest.class.getClassLoader().getResource("ip2region.xdb").toURI()).toString());
    }

    @Test
    void ipv6LoopbackUsesLocalIpv4Region() {
        String region = SmartIpUtil.getRegion("0:0:0:0:0:0:0:1");

        assertTrue(region.contains("内网IP"));
    }

    @Test
    void unsupportedIpv6ReturnsEmptyRegion() {
        String region = SmartIpUtil.getRegion("2408:8220:331:1::1");

        assertEquals(StringConst.EMPTY, region);
    }
}
