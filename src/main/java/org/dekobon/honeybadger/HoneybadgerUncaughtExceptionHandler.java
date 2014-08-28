package org.dekobon.honeybadger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Exception handler class that sends errors to Honey Badger by default.
 *
 * @author <a href="https://github.com/dekobon">dekobon</a>
 * @since 1.0.0
 */
public class HoneybadgerUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {
    protected HoneybadgerReporter reporter = new HoneybadgerReporter();
    protected Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        try {
            reporter.reportError(e);
        } catch (RuntimeException re) {
            if (logger.isErrorEnabled()) {
                logger.error("An error occurred when sending data to the " +
                             "Honeybadger API", re);
            }
        } finally {
            if (logger.isErrorEnabled()) {
                logger.error("An unhandled exception has occurred", e);
            }
        }
    }

    /**
     * Use {@link org.dekobon.honeybadger.HoneybadgerUncaughtExceptionHandler}
     * as the error handler for the current thread.
     */
    public static void registerAsUncaughtExceptionHandler() {
        Thread.UncaughtExceptionHandler handler =
                new HoneybadgerUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(handler);
    }

    /**
     * Use {@link org.dekobon.honeybadger.HoneybadgerUncaughtExceptionHandler}
     * as the error handler for the specified thread.
     *
     * @param t thread to register handler for
     */
    public static void registerAsUncaughtExceptionHandler(
            java.lang.Thread t) {
        Thread.UncaughtExceptionHandler handler =
                new HoneybadgerUncaughtExceptionHandler();
        t.setUncaughtExceptionHandler(handler);
    }
}
