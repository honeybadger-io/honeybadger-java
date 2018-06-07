package io.honeybadger.reporter.config;

import io.honeybadger.util.HBCollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * {@link ConfigContext} implementation that is used for configuring instances
 * from a Map.
 *
 * @author <a href="https://github.com/dekobon">Elijah Zupancic</a>
 * @since 1.0.10
 */
public class MapConfigContext implements ConfigContext {
    /** System property key identifying the runtime environment. */
    public static final String ENVIRONMENT_KEY =
            "ENV";

    /** System property key identifying the runtime environment. */
    public static final String JAVA_ENVIRONMENT_KEY =
            "JAVA_ENV";

    /** System property key identifying the Honeybadger URL to use. */
    public static final String HONEYBADGER_URL_KEY =
            "honeybadger.url";

    /** System property key identifying the Honeybadger API key to use. */
    public static final String HONEYBADGER_API_KEY =
            "honeybadger.api_key";

    /** System property key identifying the Honeybadger API key to use. */
    public static final String HONEYBADGER_API_KEY_ENV =
            "HONEYBADGER_API_KEY";

    /** CSV list of system properties to not include. */
    public static final String HONEYBADGER_EXCLUDED_PROPS_KEY =
            "honeybadger.excluded_sys_props";

    /** CSV list of parameters to not include. */
    public static final String HONEYBADGER_EXCLUDED_PARAMS_KEY =
            "honeybadger.excluded_params";

    /** CSV list of exception classes to ignore. */
    public static final String HONEYBADGER_EXCLUDED_CLASSES_KEY =
            "honeybadger.excluded_exception_classes";

    /** System property key that maps a package to an application. */
    public static final String APPLICATION_PACKAGE_PROP_KEY =
            "honeybadger.application_package";

    /** System property key identifying the Honeybadger Read API key. */
    public static final String READ_API_KEY_PROP_KEY =
            "honeybadger.read_api_key";

    /** Environment variable identifying the Honeybadger Read API key. */
    public static final String READ_API_KEY_ENV =
            "HONEYBADGER_READ_API_KEY";

    /** System property key indicating if we display the feedback form. */
    public static final String DISPLAY_FEEDBACK_FORM_KEY =
            "honeybadger.display_feedback_form";

    /** System property key indicating the path to the feedback form template. */
    public static final String FEEDBACK_FORM_TEMPLATE_PATH_KEY =
            "honeybadger.feedback_form_template_path";

    /** System property key indicating the proxy server. */
    public static final String HTTP_PROXY_HOST_KEY =
            "http.proxyHost";

    /** System property key indicating the proxy port. */
    public static final String HTTP_PROXY_PORT_KEY =
            "http.proxyPort";

    // I know manually adding them all sucks, but it is the simplest operation
    // for a shared library. We could do all sorts of complicated reflection
    // or annotation processing, but they are error-prone.
    /** List of all properties that we read from configuration. */
    public static final String[] ALL_PROPERTIES = {
            ENVIRONMENT_KEY, JAVA_ENVIRONMENT_KEY, HONEYBADGER_URL_KEY,
            HONEYBADGER_API_KEY, HONEYBADGER_API_KEY_ENV,
            HONEYBADGER_EXCLUDED_PROPS_KEY, HONEYBADGER_EXCLUDED_PARAMS_KEY,
            HONEYBADGER_EXCLUDED_CLASSES_KEY, APPLICATION_PACKAGE_PROP_KEY,
            READ_API_KEY_PROP_KEY, READ_API_KEY_ENV, DISPLAY_FEEDBACK_FORM_KEY,
            FEEDBACK_FORM_TEMPLATE_PATH_KEY, HTTP_PROXY_HOST_KEY,
            HTTP_PROXY_PORT_KEY
    };

    private final Map<?, ?> backingMap;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public MapConfigContext(final Map<?, ?> backingMap) {
        this.backingMap = backingMap;
    }

    @Override
    public String getEnvironment() {
        String env = normalizeEmptyAndNullAndDefaultToStringValue(ENVIRONMENT_KEY);

        if (env == null) {
            return normalizeEmptyAndNullAndDefaultToStringValue(JAVA_ENVIRONMENT_KEY);
        } else {
            return env;
        }
    }

    @Override
    public URI getHoneybadgerUrl() {
        Object value = backingMap.get(HONEYBADGER_API_KEY);

        if (value == null) return null;
        if (value instanceof URI) return (URI)value;

        if (value instanceof String) {
            String uri = normalizeEmptyAndNullAndDefaultToStringValue(HONEYBADGER_URL_KEY);
            if (uri == null) return null;

            try {
                return URI.create(uri);
            } catch (IllegalArgumentException e) {
                logger.warn("Honeybadger URL was not correctly formed. " +
                            "Property: {}", HONEYBADGER_API_KEY);
                return null;
            }
        }

        return null;
    }

    @Override
    public String getApiKey() {
        String env = normalizeEmptyAndNullAndDefaultToStringValue(HONEYBADGER_API_KEY_ENV);

        // Use either HONEYBADGER_API_KEY or the standard system property
        if (env == null) {
            return normalizeEmptyAndNullAndDefaultToStringValue(HONEYBADGER_API_KEY);
        } else {
            return env;
        }
    }

    @Override
    public Set<String> getExcludedSysProps() {
        return parseCsvStringSetOrPassOnObject(HONEYBADGER_EXCLUDED_PROPS_KEY);
    }

    @Override
    public Set<String> getExcludedParams() {
        return parseCsvStringSetOrPassOnObject(HONEYBADGER_EXCLUDED_PARAMS_KEY);
    }

    @Override
    public Set<String> getExcludedClasses() {
        return parseCsvStringSetOrPassOnObject(HONEYBADGER_EXCLUDED_CLASSES_KEY);
    }

    @Override
    public String getApplicationPackage() {
        return normalizeEmptyAndNullAndDefaultToStringValue(APPLICATION_PACKAGE_PROP_KEY);
    }

    @Override
    public String getHoneybadgerReadApiKey() {
        String env = normalizeEmptyAndNullAndDefaultToStringValue(READ_API_KEY_ENV);

        // Use either HONEYBADGER_API_KEY or the standard system property
        if (env == null) {
            return normalizeEmptyAndNullAndDefaultToStringValue(READ_API_KEY_PROP_KEY);
        } else {
            return env;
        }
    }

    @Override
    public Boolean isFeedbackFormDisplayed() {
        return parseBoolean(DISPLAY_FEEDBACK_FORM_KEY);
    }

    @Override
    public String getFeedbackFormPath() {
        return normalizeEmptyAndNullAndDefaultToStringValue(FEEDBACK_FORM_TEMPLATE_PATH_KEY);
    }

    @Override
    public String getHttpProxyHost() {
        return normalizeEmptyAndNullAndDefaultToStringValue(HTTP_PROXY_HOST_KEY);
    }

    @Override
    public Integer getHttpProxyPort() {
        Object value = backingMap.get(HTTP_PROXY_PORT_KEY);

        if (value == null) return null;
        if (value instanceof Number) return ((Number)value).intValue();

        String port = normalizeEmptyAndNullAndDefaultToStringValue(HTTP_PROXY_PORT_KEY);

        try {
            return Integer.parseInt(port);
        } catch (NumberFormatException e) {
            logger.warn("Error converting system property to integer. Property: {}",
                    HTTP_PROXY_PORT_KEY);
            return null;
        }
    }

    /**
     * Allows the caller to perform a put operation on the backing map of the
     * context. This is typically used by other {@link ConfigContext}
     * implementations that need to cobble together multiple map values.
     *
     * This method is scoped to default because no other packages should be
     * using it.
     *
     * @param key configuration key
     * @param value configuration value
     * @return return value of the put() operation from the backing map
     */
    Object put(final String key, final String value) {
        if (key == null) throw new IllegalArgumentException("Config key can't be null");
        if (key.isEmpty()) throw new IllegalArgumentException("Config key can't be blank");

        // Java generics can be stupid
        @SuppressWarnings("unchecked")
        Map<Object, Object> map = (Map<Object, Object>)this.backingMap;
        return map.put(key, value);
    }

    private String normalizeEmptyAndNullAndDefaultToStringValue(final Object key) {
        Object value = backingMap.get(key);
        if (value == null) return null;

        String stringValue = value.toString();

        if (stringValue.isEmpty()) return null;

        return stringValue;
    }

    private Boolean parseBoolean(final Object key) {
        Object value = backingMap.get(key);

        if (value == null) return null;
        if (value instanceof Boolean) return (Boolean)value;

        if (value instanceof CharSequence) {
            String stringValue = value.toString().toLowerCase();

            if (stringValue.isEmpty()) return null;

            if (stringValue.equals("true") || stringValue.equals("on")) {
                return true;
            } else if (stringValue.equals("false") || stringValue.equals("off")) {
                return false;
            } else {
                logger.warn("Unknown value for boolean with property: {}", key);
                return null;
            }
        }

        return null;
    }

    private Set<String> parseCsvStringSetOrPassOnObject(final Object key) {
        Object value = backingMap.get(key);

        if (value == null) return null;

        if (value instanceof Collection) {
            @SuppressWarnings("unchecked")
            Collection<String> collection = (Collection<String>)Collections.checkedCollection(
                    (Collection)value, String.class);
            return new HashSet<>(collection);
        }

        if (value instanceof String) {
            String stringValue = normalizeEmptyAndNullAndDefaultToStringValue(key);
            if (stringValue == null) return null;

            HashSet<String> set = new HashSet<>();
            set.addAll(HBCollectionUtils.parseNaiveCsvString(stringValue));

            return set;
        }

        logger.warn("Unknown object value for property: [key={}, value={}]",
                key, value);

        return null;
    }
}
