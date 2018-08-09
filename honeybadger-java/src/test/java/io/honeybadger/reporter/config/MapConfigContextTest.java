package io.honeybadger.reporter.config;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;

/** Verifies config properties use the expected names */
public class MapConfigContextTest {
    @Test
    public void canSetMaximumRetries() {
        Map<String, Object> configMap = ImmutableMap.of(
            "honeybadger.maximum_retry_attempts", "5"
        );
        MapConfigContext config = new MapConfigContext(configMap);
        assertEquals(5,(int)config.getMaximumErrorReportingRetries());
    }
}
