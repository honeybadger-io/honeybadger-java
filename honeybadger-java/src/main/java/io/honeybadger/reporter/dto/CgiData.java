package io.honeybadger.reporter.dto;

import java.io.Serializable;
import java.util.LinkedHashMap;

/**
 * CGI parameters passed to the server when the error occurred.
 * @author <a href="https://github.com/dekobon">Elijah Zupancic</a>
 * @since 1.0.9
 */
public class CgiData extends LinkedHashMap<String, Object>
        implements Serializable {
    private static final long serialVersionUID = 1006793090880571738L;

    public CgiData() {

    }

    public Integer getAsInteger(String key) {
        Object value = get(key);

        if (value instanceof Number) {
            return ((Number) value).intValue();
        }

        return null;
    }
}
