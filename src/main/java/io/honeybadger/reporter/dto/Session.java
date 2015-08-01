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
    public static final int MAX_SESSION_OBJ_STRING_SIZE = 4096;

    public Session() {
    }
}
