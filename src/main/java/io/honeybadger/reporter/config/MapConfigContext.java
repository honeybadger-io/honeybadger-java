package io.honeybadger.reporter.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * {@link ConfigContext} implementation that is used for configuring instances
 * from a Map.
 *
 * @author <a href="https://github.com/dekobon">Elijah Zupancic</a>
 * @since 1.0.10
 */
public class MapConfigContext implements ConfigContext {
    /** System property key identifying the Honeybadger URL to use. */
    public static final String HONEYBADGER_URL_KEY =
            "honeybadger.url";

    /** System property key identifying the Honeybadger API key to use. */
    public static final String HONEYBADGER_API_KEY =
            "honeybadger.api_key";

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

    /** System property key indicating if we display the feedback form. */
    public static final String DISPLAY_FEEDBACK_FORM_KEY =
            "honeybadger.display_feedback_form";

    /** System property key indicating the path to the feedback form template. */
    public static final String FEEDBACK_FORM_TEMPLATE_PATH_KEY =
            "honeybadger.feedback_form_template_path";

    private final Map<?, ?> backingMap;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public MapConfigContext(Map<?, ?> backingMap) {
        this.backingMap = backingMap;
    }

    @Override
    public String getHoneybadgerUrl() {
        return normalizeEmptyAndNullAndDefaultToStringValue(HONEYBADGER_URL_KEY);
    }

    @Override
    public String getApiKey() {
        return normalizeEmptyAndNullAndDefaultToStringValue(HONEYBADGER_API_KEY);
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
        return parseCsvStringSetOrPassOnObject(HONEYBADGER_EXCLUDED_PARAMS_KEY);
    }

    @Override
    public String getApplicationPackage() {
        return normalizeEmptyAndNullAndDefaultToStringValue(APPLICATION_PACKAGE_PROP_KEY);
    }

    @Override
    public String getHoneybadgerReadApiKey() {
        return normalizeEmptyAndNullAndDefaultToStringValue(READ_API_KEY_PROP_KEY);
    }

    @Override
    public Boolean isFeedbackFormDisplayed() {
        return parseBoolean(DISPLAY_FEEDBACK_FORM_KEY);
    }

    @Override
    public String getFeedbackFormPath() {
        return normalizeEmptyAndNullAndDefaultToStringValue(FEEDBACK_FORM_TEMPLATE_PATH_KEY);
    }

    private String normalizeEmptyAndNullAndDefaultToStringValue(Object key) {
        Object value = backingMap.get(key);
        if (value == null) return null;

        String stringValue = value.toString();

        if (stringValue.isEmpty()) return null;

        return stringValue;
    }

    private Boolean parseBoolean(Object key) {
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

    private Set<String> parseCsvStringSetOrPassOnObject(Object key) {
        Object value = backingMap.get(key);

        if (value instanceof Collection) {
            @SuppressWarnings("unchecked")
            Collection<String> collection = (Collection<String>)Collections.checkedCollection(
                    (Collection)value, String.class);
            Set<String> set = new HashSet<>(collection);
            return set;
        }

        if (value instanceof String) {
            String stringValue = normalizeEmptyAndNullAndDefaultToStringValue(key);
            if (stringValue == null) return null;

            HashSet<String> set = new HashSet<>();
            Collections.addAll(set, stringValue.split(","));

            return set;
        }

        logger.warn("Unknown object value for property: {}", key);
        return null;
    }
}
