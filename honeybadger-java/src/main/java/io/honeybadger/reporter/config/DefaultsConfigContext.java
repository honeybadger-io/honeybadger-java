package io.honeybadger.reporter.config;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import static io.honeybadger.reporter.config.MapConfigContext.*;

/**
 * {@link ConfigContext} implementation that outputs nothing but the default
 * values for all of the configuration settings.
 *
 * @author <a href="https://github.com/dekobon">Elijah Zupancic</a>
 * @since 1.0.10
 */
// Disabling naming/visibility checks temporarily until we can make a more philosophical decision
@SuppressWarnings({"StaticVariableName", "VisibilityModifier"})
public class DefaultsConfigContext implements ConfigContext {
    /** Default protocol for connecting to the Honeybadger API. */
    public static String DEFAULT_API_PROTO = "https";

    /** Default host to connect to for the Honeybadger API. */
    public static String DEFAULT_API_HOST = "api.honeybadger.io";

    /** Default environment name. */
    public static String DEFAULT_ENVIRONMENT = "unknown";

    /** Default Honeybadger URL. */
    private static String DEFAULT_API_URL =
            String.format("%s://%s", DEFAULT_API_PROTO, DEFAULT_API_HOST);

    /** Default feedback form template path. */
    public static String DEFAULT_FEEDBACK_FORM_TEMPLATE_PATH =
            "templates/feedback-form.mustache";

    public DefaultsConfigContext() {
    }

    @Override
    public String getEnvironment() {
        return DEFAULT_ENVIRONMENT;
    }

    /**
     * @return the default URL unless
     */
    @Override
    public URI getHoneybadgerUrl() {
        return URI.create(DEFAULT_API_URL);
    }

    @Override
    public String getApiKey() {
        return null;
    }

    @Override
    public Set<String> getExcludedSysProps() {
        HashSet<String> set = new HashSet<>();

        set.add(HONEYBADGER_API_KEY);
        set.add(READ_API_KEY_PROP_KEY);
        set.add(HONEYBADGER_EXCLUDED_PROPS_KEY);
        set.add(HONEYBADGER_URL_KEY);

        return set;
    }

    @Override
    public Set<String> getExcludedParams() {
        return new HashSet<>();
    }

    @Override
    public Set<String> getExcludedClasses() {
        return new HashSet<>();
    }

    @Override
    public String getApplicationPackage() {
        return null;
    }

    @Override
    public String getHoneybadgerReadApiKey() {
        return null;
    }

    @Override
    public Boolean isFeedbackFormDisplayed() {
        return true;
    }

    @Override
    public String getFeedbackFormPath() {
        return DEFAULT_FEEDBACK_FORM_TEMPLATE_PATH;
    }

    @Override
    public String getHttpProxyHost() {
        return null;
    }

    @Override
    public Integer getHttpProxyPort() {
        return null;
    }
}
