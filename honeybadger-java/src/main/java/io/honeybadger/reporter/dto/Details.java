package io.honeybadger.reporter.dto;

import io.honeybadger.reporter.config.ConfigContext;
import org.slf4j.MDC;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;

/**
 * Class representing metadata and run-time state.
 * @author <a href="https://github.com/dekobon">Elijah Zupancic</a>
 * @since 1.0.9
 */
public class Details extends LinkedHashMap<String, Map<String, String>>
        implements Serializable {
    private static final long serialVersionUID = -6238693264237448645L;

    private final ConfigContext config;

    public Details(final ConfigContext config) {
        this.config = config;
    }

    public Details() {
        ConfigContext defaultConfig = ConfigContext.THREAD_LOCAL.get();
        if (defaultConfig == null) throw new NullPointerException(
                "Unable to get the expected ConfigContext from ThreadLocal");
        this.config = defaultConfig;
    }

    void addDefaultDetails() {
        put("System Properties", systemProperties());
        put("MDC Properties", mdcProperties());
    }

    protected static LinkedHashMap<String, String> mdcProperties() {
        LinkedHashMap<String, String> map = new LinkedHashMap<>();

        @SuppressWarnings("unchecked")
        Map<String, String> mdc = MDC.getCopyOfContextMap();

        if (mdc != null) {
            for (Map.Entry<String, String> entry : mdc.entrySet()) {
                map.put(entry.getKey(), entry.getValue());
            }
        }

        return map;
    }

    protected Map<String, String> systemProperties() {
        TreeMap<String, String> map = new TreeMap<>();
        Set<String> excludedSysProps = config.getExcludedSysProps();

        for (Map.Entry<Object, Object> entry: System.getProperties().entrySet()) {
            final Object key = entry.getKey();

            if (key == null) {
                continue;
            }

            final String stringKey = Objects.toString(key);

            if (stringKey.isEmpty()) {
                continue;
            }

            // We skip all excluded properties
            if (excludedSysProps.contains(stringKey)) {
                continue;
            }

            map.put(stringKey, entry.getValue().toString());
        }

        return map;
    }
}
