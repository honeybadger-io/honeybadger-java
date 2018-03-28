package io.honeybadger.reporter;

import io.honeybadger.reporter.config.ConfigContext;
import io.honeybadger.reporter.config.SystemSettingsConfigContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Exception handler class that sends errors to Honey Badger by default.
 *
 * @author <a href="https://github.com/dekobon">Elijah Zupancic</a>
 * @since 1.0.0
 */
public class HoneybadgerUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {
    protected ConfigContext config;
    protected NoticeReporter reporter;
    protected Logger logger = LoggerFactory.getLogger(getClass());

    public HoneybadgerUncaughtExceptionHandler() {
        this(new SystemSettingsConfigContext());
    }

    public HoneybadgerUncaughtExceptionHandler(ConfigContext config) {
        this.config = config;
        this.reporter = new HoneybadgerReporter(config);
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        NoticeReportResult errorResult = null;

        try {
            errorResult = reporter.reportError(e);
        } catch (RuntimeException re) {
            if (logger.isErrorEnabled()) {
                logger.error("An error occurred when sending data to the " +
                             "Honeybadger API", re);
            }
        } finally {
            if (logger.isErrorEnabled()) {
                String msg = "An unhandled exception has occurred [%s]";
                String id = (errorResult == null) ?
                        "no-id" : errorResult.getId().toString();
                logger.error(String.format(msg, id), e);
            }
        }
    }

    public NoticeReporter getReporter() {
        return reporter;
    }

    public ConfigContext getConfig() {
        return config;
    }

    /**
     * Use {@link HoneybadgerUncaughtExceptionHandler}
     * as the error handler for the current thread.
     *
     * @return instance registered as handler
     */
    public static HoneybadgerUncaughtExceptionHandler registerAsUncaughtExceptionHandler() {
        final HoneybadgerUncaughtExceptionHandler handler =
                new HoneybadgerUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(handler);

        return handler;
    }

    /**
     * Use {@link HoneybadgerUncaughtExceptionHandler}
     * as the error handler for the current thread.
     *
     * @param configContext configuration context for Honeybadger setup
     */
    public static void registerAsUncaughtExceptionHandler(
            ConfigContext configContext) {
        Thread.UncaughtExceptionHandler handler =
                new HoneybadgerUncaughtExceptionHandler(configContext);
        Thread.setDefaultUncaughtExceptionHandler(handler);
    }

    /**
     * Use {@link HoneybadgerUncaughtExceptionHandler}
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

    /**
     * Use {@link HoneybadgerUncaughtExceptionHandler}
     * as the error handler for the specified thread.
     *
     * @param configContext configuration context for Honeybadger setup
     * @param t thread to register handler for
     */
    public static void registerAsUncaughtExceptionHandler(
            ConfigContext configContext, java.lang.Thread t) {
        Thread.UncaughtExceptionHandler handler =
                new HoneybadgerUncaughtExceptionHandler(configContext);
        t.setUncaughtExceptionHandler(handler);
    }
}
