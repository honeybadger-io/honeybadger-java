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
    private ConfigContext config;
    private NoticeReporter reporter;
    private Logger logger = LoggerFactory.getLogger(getClass());

    public HoneybadgerUncaughtExceptionHandler() {
        this(new SystemSettingsConfigContext());
    }

    public HoneybadgerUncaughtExceptionHandler(final ConfigContext config) {
        this.setConfig(config);
        this.setReporter(new HoneybadgerReporter(config));
    }

    @Override
    public void uncaughtException(final Thread t, final Throwable e) {
        NoticeReportResult errorResult = null;

        try {
            errorResult = getReporter().reportError(e);
        } catch (RuntimeException re) {
            if (getLogger().isErrorEnabled()) {
                getLogger().error("An error occurred when sending data to the " +
                             "Honeybadger API", re);
            }
        } finally {
            if (getLogger().isErrorEnabled()) {
                String msg = "An unhandled exception has occurred [%s]";
                String id = (errorResult == null) ?
                        "no-id" : errorResult.getId().toString();
                getLogger().error(String.format(msg, id), e);
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
            final ConfigContext configContext) {
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
            final java.lang.Thread t) {
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
            final ConfigContext configContext, final java.lang.Thread t) {
        Thread.UncaughtExceptionHandler handler =
                new HoneybadgerUncaughtExceptionHandler(configContext);
        t.setUncaughtExceptionHandler(handler);
    }

    protected void setConfig(final ConfigContext config) {
        this.config = config;
    }

    protected void setReporter(final NoticeReporter reporter) {
        this.reporter = reporter;
    }

    protected Logger getLogger() {
        return logger;
    }

    protected void setLogger(final Logger logger) {
        this.logger = logger;
    }
}
