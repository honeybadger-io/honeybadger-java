package io.honeybadger.reporter.config;

/**
 * Implementation of {@link ConfigContext} that inherits from environment
 * variables and from Java system properties.
 *
 * @author <a href="https://github.com/dekobon">Elijah Zupancic</a>
 * @since 1.0.10
 */
public class SystemSettingsConfigContext extends BaseChainedConfigContext {
    /**
     * Populate configuration from defaults, environment variables and system
     * properties.
     */
    public SystemSettingsConfigContext() {
        // load defaults
        super();
        // overwrite with environment variables
        overwriteWithContext(new MapConfigContext(System.getenv()));
        // overwrite with system properties
        overwriteWithContext(new MapConfigContext(System.getProperties()));
    }

    /**
     * Populate configuration from defaults, environment variables, system
     * properties and an addition context passed in.
     *
     * @param context additional context to layer on top
     */
    public SystemSettingsConfigContext(final ConfigContext context) {
        // load all of the chained defaults
        this();
        // now load in an additional context
        overwriteWithContext(context);
    }
}
