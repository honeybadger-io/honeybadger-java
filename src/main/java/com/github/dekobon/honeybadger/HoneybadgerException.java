package com.github.dekobon.honeybadger;

/**
 * Exception class representing an error state with Honeybadger error reporting.
 *
 * @author <a href="https://github.com/dekobon">dekobon</a>
 * @since 1.0.0
 */
@SuppressWarnings("serial")
public class HoneybadgerException extends RuntimeException {
    public HoneybadgerException() {
    }

    public HoneybadgerException(String message) {
        super(message);
    }

    public HoneybadgerException(String message, Throwable cause) {
        super(message, cause);
    }

    public HoneybadgerException(Throwable cause) {
        super(cause);
    }

    public HoneybadgerException(String message, Throwable cause,
                                boolean enableSuppression,
                                boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
