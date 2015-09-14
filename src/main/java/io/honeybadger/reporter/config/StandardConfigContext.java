package io.honeybadger.reporter.config;

import java.util.Map;

/**
 * @{link ConfigContext} implementation that is used for programmatic
 * configuration. This implementation completely ignores all environment
 * variables and system parameters.
 *
 * @author <a href="https://github.com/dekobon">Elijah Zupancic</a>
 * @since 1.0.10
 */
public class StandardConfigContext extends BaseChainedConfigContext {
    public StandardConfigContext() {
        super();
    }

    public StandardConfigContext(Map<String, ?> configuration) {
        super();
        overwriteWithContext(new MapConfigContext(configuration));
    }
}
