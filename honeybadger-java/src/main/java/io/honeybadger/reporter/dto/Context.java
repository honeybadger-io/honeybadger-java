package io.honeybadger.reporter.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;
import java.util.LinkedHashMap;

/**
 * The context of an HTTP request.
 * @author <a href="https://github.com/dekobon">Elijah Zupancic</a>
 * @since 1.0.9
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Context extends LinkedHashMap<String, String>
        implements Serializable {
    private static final long serialVersionUID = -5418699300879809188L;

    public Context() {
    }

    public Context setUsername(final String username) {
        put("user_name", username);
        return this;
    }
}
