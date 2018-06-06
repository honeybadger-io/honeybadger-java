package io.honeybadger.reporter;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import io.honeybadger.reporter.config.ConfigContext;

/**
 * {@link ExclusionStrategy} that excludes {@link ConfigContext} instances.
 *
 * @author <a href="https://github.com/dekobon">Elijah Zupancic</a>
 * @since 1.0.10
 */
public class HoneybadgerExclusionStrategy implements ExclusionStrategy {
    @Override
    public boolean shouldSkipField(final FieldAttributes f) {
        return f.getName().equals("excludedValues");
    }

    @Override
    public boolean shouldSkipClass(final Class<?> clazz) {
        return clazz.isAssignableFrom(ConfigContext.class);
    }
}
