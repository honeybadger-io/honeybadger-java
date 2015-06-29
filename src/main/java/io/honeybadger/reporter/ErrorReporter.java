package io.honeybadger.reporter;

import java.util.UUID;

/**
 * Interface representing error reporting behavior.
 *
 * @author Elijah Zupancic
 * @since 1.0.9
 */
public interface ErrorReporter {
    /**
     * Send any Java {@link java.lang.Throwable} to the Honeybadger error
     * reporting interface.
     *
     * @param error error to report
     * @return UUID of error created, if there was a problem null
     */
    UUID reportError(Throwable error);

    /**
     * Send any Java {@link java.lang.Throwable} to the Honeybadger error
     * reporting interface.
     *
     * @param error error to report
     * @param request Object to parse for request properties
     * @return UUID of error created, if there was a problem or ignored null
     */
    UUID reportError(Throwable error, Object request);
}
