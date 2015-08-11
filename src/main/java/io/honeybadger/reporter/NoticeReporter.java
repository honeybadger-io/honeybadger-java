package io.honeybadger.reporter;

/**
 * Interface representing error reporting behavior.
 *
 * @author <a href="https://github.com/dekobon">Elijah Zupancic</a>
 * @since 1.0.9
 */
public interface NoticeReporter {
    /** System property key identifying the Honeybadger URL to use. */
    String HONEYBADGER_URL_SYS_PROP_KEY =
            "honeybadger.url";
    /** System property key identifying the Honeybadger API key to use. */
    String HONEYBADGER_API_KEY_SYS_PROP_KEY =
            "honeybadger.api_key";

    /** CSV list of system properties to not include. */
    String HONEYBADGER_EXCLUDED_PROPS_SYS_PROP_KEY =
            "honeybadger.excluded_sys_props";

    /** CSV list of exception classes to ignore. */
    String HONEYBADGER_EXCLUDED_CLASSES_SYS_PROP_KEY =
            "honeybadger.excluded_exception_classes";

    /** System property key that maps a package to an application. */
    String APPLICATION_PACKAGE_PROP_KEY =
            "honeybadger.application_package";

    /** System property key identifying the Honeybadger Read API key. */
    String READ_API_KEY_PROP_KEY =
            "honeybadger.read_api_key";

    String DEFAULT_API_URI =
            "https://api.honeybadger.io/v1/notices";

    /**
     * Send any Java {@link java.lang.Throwable} to the Honeybadger error
     * reporting interface.
     *
     * @param error error to report
     * @return UUID of error created, if there was a problem null
     */
    NoticeReportResult reportError(Throwable error);

    /**
     * Send any Java {@link java.lang.Throwable} to the Honeybadger error
     * reporting interface.
     *
     * @param error error to report
     * @param request Object to parse for request properties
     * @return UUID of error created, if there was a problem or ignored null
     */
    NoticeReportResult reportError(Throwable error, Object request);
}
