package io.honeybadger.reporter;

/**
 * Interface representing error reporting behavior.
 *
 * @author <a href="https://github.com/dekobon">Elijah Zupancic</a>
 * @since 1.0.9
 */
public interface NoticeReporter {
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
