package io.honeybadger.reporter.dto;

import io.honeybadger.reporter.config.ConfigContext;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Class representing parameters requested when an exception occurred.
 * @author <a href="https://github.com/dekobon">Elijah Zupancic</a>
 * @since 1.0.9
 */
public class Params extends LinkedHashMap<String, String>
        implements Serializable {
    private static final long serialVersionUID = -5633548926144410598L;
    private final Set<String> excludedValues;

    public Params(final Set<String> excludedValues) {
        this.excludedValues = excludedValues;
    }

    @Deprecated
    public Params() {
        ConfigContext config = ConfigContext.THREAD_LOCAL.get();
        if (config == null) throw new NullPointerException(
                "Unable to get the expected ConfigContext from ThreadLocal");

        this.excludedValues = config.getExcludedParams();
    }

    /**
     * Converts multiple HTTP parameters into a CSV format.
     * @param strings parameters to convert
     * @return CSV of params, otherwise empty string
     */
    static String csv(final String[] strings) {
        if (strings == null || strings.length == 0) return "";
        if (strings.length == 1) return strings[0];

        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < strings.length; i++) {
            builder.append(strings[i]);
            if (i < strings.length - 1) builder.append(", ");
        }

        return builder.toString();
    }

    @Override
    public String put(final String key, final String value) {
        if (excludedValues.contains(key)) {
            return null;
        }

        return super.put(key, value);
    }

    static Params parseParamsFromMap(final Set<String> excludedValues,
                                     final Map<String, String[]> paramMap) {
        Params params = new Params(excludedValues);

        try {
            if (paramMap == null || paramMap.isEmpty()) return params;

            for (Map.Entry<String, String[]> entry : paramMap.entrySet()) {
                params.put(entry.getKey(), Params.csv(entry.getValue()));
            }
        } catch (RuntimeException e) {
            /* We really shouldn't ever have an exception here, but we can't
             * control the underlying implementation, so we just recover by
             * not displaying any data. */

            params.put("Error getting parameters", e.getMessage());
        }

        return params;
    }
}
