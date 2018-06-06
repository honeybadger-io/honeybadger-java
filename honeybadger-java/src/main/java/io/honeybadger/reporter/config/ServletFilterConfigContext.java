package io.honeybadger.reporter.config;

import javax.servlet.FilterConfig;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * {@link ConfigContext} implementation that reads its configuration from
 * a servlet {@link FilterConfig}.
 *
 * @author <a href="https://github.com/dekobon">Elijah Zupancic</a>
 * @since 1.0.4
 */
public class ServletFilterConfigContext extends MapConfigContext {

    public ServletFilterConfigContext(final FilterConfig filterConfig) {
        super(asMap(filterConfig));
    }

    /**
     * Converts a servlet {@link FilterConfig} to a {@link Map}.
     * @param filterConfig filter configuration to convert
     * @return filter configuration as map
     */
    protected static Map<String, String> asMap(final FilterConfig filterConfig) {
        Map<String, String> map = new HashMap<>();
        Enumeration<String> enumeration = filterConfig.getInitParameterNames();

        while (enumeration.hasMoreElements()) {
            String key = enumeration.nextElement();
            if (key == null || key.isEmpty()) continue;
            String value = filterConfig.getInitParameter(key);
            if (value == null || value.isEmpty()) continue;

            map.put(key, value);
        }

        return map;
    }
}
