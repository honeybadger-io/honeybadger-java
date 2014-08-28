package org.dekobon.honeybadger;

/**
 * Exception handler class that sends errors to Honey Badger by default.
 *
 * @author Elijah Zupancic
 * @since 1.0.0
 */
public class HoneybadgerUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {
    protected HoneybadgerReporter reporter = new HoneybadgerReporter();

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        reporter.reportErrorToHoneyBadger(e);
    }
}
