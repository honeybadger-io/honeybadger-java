package io.honeybadger.reporter.config;

import java.net.URI;
import java.util.Set;

/**
 * Interface defining the common properties needed to configure a
 * {@link io.honeybadger.reporter.NoticeReporter}.
 *
 * @author <a href="https://github.com/dekobon">Elijah Zupancic</a>
 * @since 1.0.10
 */
public interface ConfigContext {
    /** Global reference to the currently used config per thread. */
    ThreadLocal<ConfigContext> THREAD_LOCAL = new ThreadLocal<>();

    /** @return name of environment */
    String getEnvironment();

    /** @return Honeybadger URL to use */
    URI getHoneybadgerUrl();

    /** @return Honeybadger API key to use */
    String getApiKey();

    /** @return Set of system properties to not include */
    Set<String> getExcludedSysProps();

    /** @return Set of parameters to not include */
    Set<String> getExcludedParams();

    /** @return Set of exception classes to ignore */
    Set<String> getExcludedClasses();

    /** @return String that maps a package to an application */
    String getApplicationPackage();

    /** @return Honeybadger Read API key */
    String getHoneybadgerReadApiKey();

    /** @return Do we display the feedback form? */
    Boolean isFeedbackFormDisplayed();

    /** @return The path to the feedback form template */
    String getFeedbackFormPath();

    /** @return Host of proxy server to send traffic through */
    String getHttpProxyHost();

    /** @return Port of proxy server to send traffic through */
    Integer getHttpProxyPort();
}
