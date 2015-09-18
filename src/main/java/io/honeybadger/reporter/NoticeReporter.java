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

    /** Environment variable identifying Honeybadger API key to use. */
    String HONEYBADGER_API_KEY = "HONEYBADGER_API_KEY";

    /** CSV list of system properties to not include. */
    String HONEYBADGER_EXCLUDED_PROPS_SYS_PROP_KEY =
            "honeybadger.excluded_sys_props";

    /** CSV list of parameters to not include. */
    String HONEYBADGER_EXCLUDED_PARAMS_SYS_PROP_KEY =
            "honeybadger.excluded_params";

    /** CSV list of exception classes to ignore. */
    String HONEYBADGER_EXCLUDED_CLASSES_SYS_PROP_KEY =
            "honeybadger.excluded_exception_classes";

    /** System property key that maps a package to an application. */
    String APPLICATION_PACKAGE_PROP_KEY =
            "honeybadger.application_package";

    /** System property key identifying the Honeybadger Read API key. */
    String READ_API_KEY_PROP_KEY =
            "honeybadger.read_api_key";

    /** System property key indicating if we display the feedback form. */
    String DISPLAY_FEEDBACK_FORM_KEY =
            "honeybadger.display_feedback_form";

    /** System property key indicating the path to the feedback form template. */
    String FEEDBACK_FORM_TEMPLATE_PATH_KEY =
            "honeybadger.feedback_form_template_path";

    /** Default feedback form template path. */
    String DEFAULT_FEEDBACK_FORM_TEMPLATE_PATH = "templates/feedback-form.mustache";

    /** Default protocol for connecting to the Honeybadger API. */
    String DEFAULT_API_PROTO = "https";

    /** Default host to connect to for the Honeybadger API. */
    String DEFAULT_API_HOST = "api.honeybadger.io";

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
