package io.honeybadger.reporter.dto;

import java.util.Map;

/**
 * Common utilities used for parsing HTTP request data.
 * @author <a href="https://github.com/dekobon">Elijah Zupancic</a>
 * @since 1.0.9
 */
class RequestParsingUtils {
    static Params parseParamsFromMap(Map<String, String[]> paramMap) {
        Params params = new Params();

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
