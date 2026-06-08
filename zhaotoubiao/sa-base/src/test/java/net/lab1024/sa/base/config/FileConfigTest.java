package net.lab1024.sa.base.config;

import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileConfigTest {

    @Test
    void localResourceLocationUsesPlatformFileUri() {
        String uploadPath = "/home/zhaotoubiao/upload/";

        String location = FileConfig.buildLocalResourceLocation(uploadPath);

        assertEquals(new File(uploadPath).toURI().toString(), location);
    }

    @Test
    void localResourceLocationKeepsDirectorySeparator() {
        String uploadPath = "/home/zhaotoubiao/upload";

        String location = FileConfig.buildLocalResourceLocation(uploadPath);

        assertTrue(location.endsWith("/"));
    }
}
