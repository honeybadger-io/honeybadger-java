package io.honeybadger.reporter.dto;

import java.io.Serializable;
import java.util.LinkedHashMap;

/**
 * Session store when error was triggered.
 * @author <a href="https://github.com/dekobon">Elijah Zupancic</a>
 * @since 1.0.9
 */
public class Session extends LinkedHashMap<String, Object>
        implements Serializable {
    private static final long serialVersionUID = 1683674267395812181L;
    /** The maximum amount of characters to dump for a session object. **/
    public static final int MAX_SESSION_OBJ_STRING_SIZE = 4096;

    public Session() {
    }

    @Override
    public Object put(final String key, final Object value) {
        if (key.equals("creation_time")) {
            if (value instanceof CharSequence) {
                return super.put(key, Long.parseLong(value.toString()));
            } else if (value instanceof Number) {
                return super.put(key, ((Number)value).longValue());
            }
        }

        return super.put(key, value);
    }
}
