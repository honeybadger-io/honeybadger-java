package io.honeybadger.reporter.dto;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.Serializable;
import java.util.Enumeration;
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
    private static final int MAX_SESSION_OBJ_STRING_SIZE = 4096;

    public Session(HttpServletRequest request) {
        addSessionElements(request);
    }

    void addSessionElements(HttpServletRequest request) {
        final HttpSession session = request.getSession();

        if (session == null) return;

        try {
            put("session_id", session.getId());
            put("creation_time", session.getCreationTime());

            final Enumeration<String> attributes = session.getAttributeNames();

            while (attributes.hasMoreElements()) {
                final String key = attributes.nextElement();
                final Object value = session.getAttribute(key);
                final String valueAsString = String.valueOf(value).substring(0, MAX_SESSION_OBJ_STRING_SIZE);

                put(key, valueAsString);
            }
        } catch (RuntimeException e) {
           put("Error getting session", e.getMessage());
        }
    }
}
