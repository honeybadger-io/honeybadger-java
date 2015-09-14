/**
 * Configuration class are contained in this package.
 *
 * The usage pattern is that you will define an implementation of
 * @{link ConfigContext} that provides a default values implementation
 * and then you will subclass {@link io.honeybadger.reporter.config.BaseChainedConfigContext}
 * and use the method overwriteWithContext() to load the default values into
 * the class and then load that specific configuration's values on top of the
 * default values.
 *
 * @author <a href="https://github.com/dekobon">Elijah Zupancic</a>
 * @since 1.0.10
 */
package io.honeybadger.reporter.config;
