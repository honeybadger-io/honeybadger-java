package io.honeybadger.reporter;

/**
 * Exception class representing an error state with Honeybadger error reporting.
 *
 * @author <a href="https://github.com/dekobon">Elijah Zupancic</a>
 * @since 1.0.0
 */
@SuppressWarnings("serial")
public class HoneybadgerException extends RuntimeException {
    public HoneybadgerException() {
    }

    public HoneybadgerException(final String message) {
        super(message);
    }

    public HoneybadgerException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public HoneybadgerException(final Throwable cause) {
        super(cause);
    }

    public HoneybadgerException(final String message, final Throwable cause,
                                final boolean enableSuppression,
                                final boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
