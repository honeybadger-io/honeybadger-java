package io.honeybadger.reporter.config;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * {@link ConfigContext} implementation that is populated by values taken from
 * the Spring Framework.
 *
 * @author <a href="https://github.com/dekobon">Elijah Zupancic</a>
 * @since 1.0.11
 */
@Component
public class SpringConfigContext extends BaseChainedConfigContext {
    @Autowired
    public SpringConfigContext(final Environment environment) {
        super();

        if (environment != null) {
            Map<String, String> mapProps = environmentAsMap(environment);
            MapConfigContext mapConfigContext = new MapConfigContext(mapProps);

            overwriteWithContext(mapConfigContext);
        } else {
            LoggerFactory.getLogger(getClass()).warn(
                "Null Spring environment. Using defaults and system settings."
            );
            overwriteWithContext(new SystemSettingsConfigContext());
        }
    }

    /**
     * Spring's {@link Environment} class doesn't provide a way to get all of
     * the runtime configuration properties as a {@link Map}, so this helper
     * method iterates through all known keys and queries Spring for values.
     *
     * @param environment Spring environment class
     * @return Map containing all properties found in Spring
     */
    protected static Map<String, String> environmentAsMap(
            final Environment environment) {
        if (environment == null) {
            String msg = "Spring environment must not be null";
            throw new IllegalArgumentException(msg);
        }

        final String[] keys = MapConfigContext.ALL_PROPERTIES;
        final Map<String, String> map = new HashMap<>(keys.length);

        for (String key : keys) {
            String value = environment.getProperty(key);
            if (value == null) continue;

            map.put(key, value);
        }

        return map;
    }
}
