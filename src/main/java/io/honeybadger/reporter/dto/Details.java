package io.honeybadger.reporter.dto;

import com.google.gson.annotations.Expose;
import io.honeybadger.reporter.config.ConfigContext;
import org.slf4j.MDC;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Class representing metadata and run-time state.
 * @author <a href="https://github.com/dekobon">Elijah Zupancic</a>
 * @since 1.0.9
 */
public class Details extends LinkedHashMap<String, LinkedHashMap<String, String>>
        implements Serializable {
    private static final long serialVersionUID = -6238693264237448645L;

    private final ConfigContext config;

    public Details(ConfigContext config) {
        this.config = config;
        addDefaultDetails();
    }

    public Details() {
        ConfigContext config = ConfigContext.threadLocal.get();
        if (config == null) throw new NullPointerException(
                "Unable to get the expected ConfigContext from ThreadLocal");
        this.config = config;

        addDefaultDetails();
    }

    protected void addDefaultDetails() {
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

    protected LinkedHashMap<String, String> systemProperties() {
        LinkedHashMap<String, String> map = new LinkedHashMap<>();
        Set<String> excludedSysProps = config.getExcludedSysProps();

        for (Map.Entry<Object, Object> entry: System.getProperties().entrySet()) {
            // We skip all excluded properties
            if (excludedSysProps.contains(entry.getKey().toString())) {
                continue;
            }

            map.put(entry.getKey().toString(),
                    entry.getValue().toString());
        }

        return map;
    }
}
