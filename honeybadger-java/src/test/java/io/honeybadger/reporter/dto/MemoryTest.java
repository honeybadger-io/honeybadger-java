package io.honeybadger.reporter.dto;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.net.URL;
import java.util.Map;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assume.assumeThat;

public class MemoryTest {

    @Test
    public void canFindLinuxMemInfo() throws IOException {
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        String os = osBean.getName();

        assumeThat(os, is("Linux"));

        Map<String, Long> memInfo = Memory.findLinuxMemInfo(findTestFile());

        assertEquals("MemTotal should be hardcoded value of 32117 mebibytes",
                     32117L, (long)memInfo.get("MemTotal"));
        assertEquals("MemFree should be hardcoded value of 408 mebibytes",
                408L, (long)memInfo.get("MemFree"));
        assertEquals("Buffers should be hardcoded value of 408 mebibytes",
                613L, (long)memInfo.get("Buffers"));
        assertEquals("Cached should be hardcoded value of 408 mebibytes",
                15275L, (long)memInfo.get("Cached"));
        assertEquals("FreeTotal should be hardcoded value of 408 mebibytes",
                16296L, (long)memInfo.get("FreeTotal"));
    }

    private File findTestFile() throws IOException {
        URL url = getClass().getClassLoader().getResource("test_meminfo");
        File urlFile = new File(url.getFile());
        if (urlFile.exists() && urlFile.canRead()) return urlFile;

        File rootPath = new File("./src/test/resources/test_meminfo");
        if (rootPath.exists() && rootPath.canRead()) return rootPath;

        throw new IOException("Can't find test_meminfo test data");
    }
}
