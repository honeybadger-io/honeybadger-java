package io.honeybadger.reporter.config;

import java.util.Map;

/**
 * {@link ConfigContext} implementation that is used for programmatic
 * configuration. This implementation completely ignores all environment
 * variables and system parameters.
 *
 * @author <a href="https://github.com/dekobon">Elijah Zupancic</a>
 * @since 1.0.10
 */
public class StandardConfigContext extends BaseChainedConfigContext {
    /**
     * A new configuration context with default values prepopulated.
     */
    public StandardConfigContext() {
        super();
    }

    /**
     * A new configuration context with default values present, but overwritten
     * by the passed map of configuration values.
     *
     * @param configuration map with keys that correspond to the sys prop keys documented
     */
    public StandardConfigContext(final Map<String, ?> configuration) {
        super();
        overwriteWithContext(new MapConfigContext(configuration));
    }

    /**
     * A new configuration context with the default values present. Only the
     * API key is set.
     *
     * @param apiKey Honeybadger API key
     */
    public StandardConfigContext(final String apiKey) {
        if (apiKey == null) throw new IllegalArgumentException(
                "API key must not be null");
        if (apiKey.isEmpty()) throw new IllegalArgumentException(
                "API key must not be empty");

        setApiKey(apiKey);
    }
}
