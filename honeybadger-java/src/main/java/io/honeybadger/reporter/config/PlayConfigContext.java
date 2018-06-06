package io.honeybadger.reporter.config;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigValue;
import io.honeybadger.util.HBStringUtils;
import org.slf4j.LoggerFactory;
import play.Environment;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * {@link ConfigContext} implementation that reads its configuration values from
 * a Play Framework {@link Config} class.
 *
 * @author <a href="https://github.com/dekobon">Elijah Zupancic</a>
 * @since 1.0.11
 */
public class PlayConfigContext extends BaseChainedConfigContext {
    /**
     * {@link Map.Entry} implementation for mapping between Play config objects
     * and generic Map.Entry objects.
     *
     * @param <K> key type
     * @param <V> value type
     */
    private static final class ConfigEntry<K, V> implements Map.Entry<K, V> {
        private final K key;
        private V val;

        static Map.Entry<String, Object> fromConfigValueEntry(
                final Map.Entry<String, ConfigValue> entry) {
            return new ConfigEntry<>(entry.getKey(), entry.getValue().unwrapped());
        }

        ConfigEntry(final K key, final V val) {
            this.key = key;
            this.val = val;
        }

        @Override
        public K getKey() {
            return key;
        }

        @Override
        public V getValue() {
            return val;
        }

        @Override
        public V setValue(final V value) {
            final V oldValue = val;
            this.val = value;
            return oldValue;
        }
    }
    /**
     * Converts a Play Configuration to a Honeybadger {@link ConfigContext}. If
     * null, it will default to a {@link SystemSettingsConfigContext}.
     *
     * @param configuration play configuration to convert
     * @param environment play environment to load values from
     */
    public PlayConfigContext(final Config configuration,
                             final Environment environment) {
        super();

        final String env;

        if (environment == null) {
            env = "UNKNOWN";
        } else if (environment.mode().name() == null || environment.mode().name().isEmpty()) {
            env = "UNKNOWN";
        } else {
            env = environment.mode().name();
        }

        if (configuration != null) {
            final Map<String, Object> unwrapped;

            try (Stream<Map.Entry<String, ConfigValue>> stream = configuration.entrySet().stream()) {
                unwrapped = stream.map(ConfigEntry::fromConfigValueEntry)
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            }

            Map<String, Object> configMap = flattenNestedMap(unwrapped);
            MapConfigContext mapContext = new MapConfigContext(configMap);
            // Overload environment value because it won't be present in the
            // config map.
            mapContext.put(MapConfigContext.ENVIRONMENT_KEY, env);

            overwriteWithContext(mapContext);
        } else {
            LoggerFactory.getLogger(getClass()).warn(
                    "Null Play configuration. Using defaults and system settings.");
            overwriteWithContext(new SystemSettingsConfigContext());
        }
    }

    /**
     * Flattens a hierarchical map of nested maps into a single map with each
     * nesting delineated by a dot. Assumes a nesting level of zero.
     * @param map nested map
     * @return a flat map
     */
    static Map<String, Object> flattenNestedMap(final Map<String, Object> map) {
        return flattenNestedMap(map, 0);
    }

    /**
     * Flattens a hierarchical map of nested maps into a single map with each
     * nesting delineated by a dot.
     * @param map nested map
     * @param level level of nesting
     * @return a flat map
     */
    static Map<String, Object> flattenNestedMap(final Map<String, Object> map, final long level) {
        if (map.isEmpty()) return Collections.emptyMap();

        Map<String, Object> flat = new HashMap<>();

        for (final Map.Entry<String, Object> entry : map.entrySet()) {
            if (entry.getKey() == null || entry.getKey().isEmpty()) continue;

            if (entry.getValue() instanceof Map) {
                // assume that we have the same underlying generic definition
                @SuppressWarnings("unchecked")
                Map<String, Object> innerMap = (Map<String, Object>) entry.getValue();

                // Don't bother to continue processing if this config section is empty
                if (innerMap.isEmpty()) continue;

                Map<String, Object> subFlat = flattenNestedMap(innerMap, level + 1);

                Iterator<Map.Entry<String, Object>> subItr = subFlat.entrySet().iterator();

                while (subItr.hasNext()) {
                    Map.Entry<String, Object> subEntry = subItr.next();
                    String subKey = HBStringUtils.stripTrailingChar(subEntry.getKey(), '.');
                    String key = String.format("%s.%s", entry.getKey(), subKey);
                    flat.put(key, subEntry.getValue());
                }
            } else if (level == 0) {
                String key = HBStringUtils.stripTrailingChar(entry.getKey(), '.');
                flat.put(String.format("%s", key), entry.getValue());
            } else {
                String key = HBStringUtils.stripTrailingChar(entry.getKey(), '.');
                flat.put(String.format("%s.", key), entry.getValue());
            }
        }

        return flat;
    }
}
