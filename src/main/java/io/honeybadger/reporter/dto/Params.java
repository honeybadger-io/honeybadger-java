package io.honeybadger.reporter.dto;

import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Class representing parameters requested when an exception occurred.
 */
public class Params extends LinkedHashMap<String, String>
        implements Serializable {
    private static final long serialVersionUID = -5633548926144410598L;

    public Params(HttpServletRequest request) {
        addParams(request);
    }

    protected void addParams(HttpServletRequest request) {
        try {
            Map<String, String[]> paramMap = request.getParameterMap();

            if (paramMap == null || paramMap.isEmpty()) return;

            for (Map.Entry<String, String[]> entry : paramMap.entrySet()) {
                put(entry.getKey(), csv(entry.getValue()));
            }
        } catch (RuntimeException e) {
            /* We really shouldn't ever have an exception here, but we can't
             * control the underlying implementation, so we just recover by
             * not displaying any data. */

            put("Error getting parameters", e.getMessage());
        }
    }

    static String csv(String[] strings) {
        if (strings == null || strings.length == 0) return "";
        if (strings.length == 1) return strings[0];

        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < strings.length; i++) {
            builder.append(strings[i]);
            if (i < strings.length - 1) builder.append(", ");
        }

        return builder.toString();
    }
}
