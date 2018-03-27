package io.honeybadger.reporter.config;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;

@SuppressWarnings("unchecked")
public class PlayConfigContextTest {
    @Test
    public void canFlattenASingleLevel() {
        Map<String, Object> instance = ImmutableMap.of(
                "key_1", "value_1",
                "key_2", "value_2"
        );

        Map<String, Object> flat = PlayConfigContext.flattenNestedMap(instance);

        try {
            assertEquals("First level keys/vals should map",
                    "value_1", flat.get("key_1"));
            assertEquals("First level keys/vals should map",
                    "value_2", flat.get("key_2"));
        } catch (AssertionError e) {
            System.err.println("Actual value of flattened map: " +
                    StringUtils.join(flat.entrySet()));
            throw e;
        }
    }

    @Test
    public void canFlattenTwoLevels() {
        Map<String, Object> instance = ImmutableMap.of(
                "key_1", ImmutableMap.of("key_a", "value_a",
                                                 "key_b", "value_b"),
                "key_2", ImmutableMap.of("key_c", "value_c",
                                                 "key_d", "value_d")
        );

        Map<String, Object> flat = PlayConfigContext.flattenNestedMap(instance);

        try {
            assertEquals("Second level keys/vals should map",
                         "value_a", flat.get("key_1.key_a"));
            assertEquals("Second level keys/vals should map",
                         "value_b", flat.get("key_1.key_b"));
            assertEquals("Second level keys/vals should map",
                         "value_c", flat.get("key_2.key_c"));
            assertEquals("Second level keys/vals should map",
                         "value_d", flat.get("key_2.key_d"));
        } catch (AssertionError e) {
            System.err.println("Actual value of flattened map: " +
                    StringUtils.join(flat.entrySet()));
            throw e;
        }
    }

    @Test
    public void canFlattenOneAndTwoLevels() {
        Map<String, Object> instance = ImmutableMap.of(
                "key_1", ImmutableMap.of("key_a", "value_a",
                        "key_b", "value_b"),
                "key_2", "value_c",
                "key_3", ImmutableMap.of("key_d", "value_d",
                        "key_e", "value_e")
        );

        Map<String, Object> flat = PlayConfigContext.flattenNestedMap(instance);

        try {
            assertEquals("Second level keys/vals should map",
                    "value_a", flat.get("key_1.key_a"));
            assertEquals("Second level keys/vals should map",
                    "value_b", flat.get("key_1.key_b"));
            assertEquals("First level keys/vals should map",
                    "value_c", flat.get("key_2"));
            assertEquals("Second level keys/vals should map",
                    "value_d", flat.get("key_3.key_d"));
            assertEquals("Second level keys/vals should map",
                    "value_e", flat.get("key_3.key_e"));
        } catch (AssertionError e) {
            System.err.println("Actual value of flattened map: " +
                    StringUtils.join(flat.entrySet()));
            throw e;
        }
    }

    @Test
    public void canFlattenThreeLevels() {
        Map<String, Object> instance = ImmutableMap.of(
                "key_1", ImmutableMap.of("key_a", (Object)ImmutableMap.of("key_b", "value_a")),
                "key_2", ImmutableMap.of("key_c", ImmutableMap.of("key_d", "value_b"))
        );

        Map<String, Object> flat = PlayConfigContext.flattenNestedMap(instance);

        try {
            assertEquals("Third level keys/vals should map",
                    "value_a", flat.get("key_1.key_a.key_b"));
            assertEquals("Second level keys/vals should map",
                    "value_b", flat.get("key_2.key_c.key_d"));
        } catch (AssertionError e) {
            System.err.println("Actual value of flattened map: " +
                    StringUtils.join(flat.entrySet()));
            throw e;
        }
    }
}
