package io.honeybadger.reporter.dto;

import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;
import java.security.Principal;
import java.util.LinkedHashMap;

/**
 * The context of an HTTP request.
 * @author <a href="https://github.com/dekobon">Elijah Zupancic</a>
 * @since 1.0.9
 */
public class Context extends LinkedHashMap<String, String>
        implements Serializable {
    private static final long serialVersionUID = -5418699300879809188L;

    public Context() {
    }
}
