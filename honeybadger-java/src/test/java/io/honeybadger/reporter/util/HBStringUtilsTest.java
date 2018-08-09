package io.honeybadger.reporter.util;

import io.honeybadger.util.HBStringUtils;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class HBStringUtilsTest {

    @Test
    public void canTrimTrailingDot() {
        String instance = "foo.";
        String out = HBStringUtils.stripTrailingChar(instance, '.');
        assertEquals("Trailing dot not trimmed", "foo", out);
    }

    @Test
    public void wontTrimOtherChars() {
        String instance = "foo";
        String out = HBStringUtils.stripTrailingChar(instance, '.');
        assertEquals("Original string modified", "foo", out);
    }
}
