package io.honeybadger.reporter.dto;

import io.honeybadger.reporter.config.ConfigContext;

import java.io.Serializable;

/**
 * One single line on a backtrace.
 * @author <a href="https://github.com/dekobon">Elijah Zupancic</a>
 * @since 1.0.9
 */
public class BacktraceElement implements Serializable {
    private static final long serialVersionUID = -4455225669072193184L;

    /**
     * Enum representing all of the valid context values on the Honeybadger API.
     */
    static enum Context {
        /** Backtrace not-belonging to the calling application. **/
        ALL("all"),
        /** Backtrace belonging to the calling application. **/
        APP("app");

        private final String name;

        Context(String name) {
            this.name = name;
        }

        String getName() {
            return this.name;
        }
    }
    private final ConfigContext config;
    public final String file;
    public final String method;
    public final String number;
    public final String context;

    public BacktraceElement(ConfigContext config, String number, String file,
                            String method) {
        this.config = config;
        this.number = number;
        this.file = file;
        this.method = method;
        this.context = calculateContext(method).getName();
    }

    public BacktraceElement(ConfigContext config, StackTraceElement element) {
        this.config = config;
        this.number = String.valueOf(element.getLineNumber());
        this.file = String.valueOf(element.getFileName());
        this.method = formatMethod(element);
        this.context = calculateContext(method).getName();
    }

    static String formatMethod(StackTraceElement element) {
        return String.format("%s.%s",
                element.getClassName(), element.getMethodName());
    }

    Context calculateContext(String method) {
        final String appPackage = config.getApplicationPackage();
        final Context methodContext;

        if (appPackage == null || appPackage.isEmpty()) {
            methodContext = Context.ALL;
        } else if (method == null || method.isEmpty()) {
            methodContext = Context.ALL;
        } else if (method.startsWith(appPackage)) {
            methodContext = Context.APP;
        } else {
            methodContext = Context.ALL;
        }

        return methodContext;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BacktraceElement that = (BacktraceElement) o;

        if (file != null ? !file.equals(that.file) : that.file != null) return false;
        if (method != null ? !method.equals(that.method) : that.method != null) return false;
        if (number != null ? !number.equals(that.number) : that.number != null) return false;
        return !(context != null ? !context.equals(that.context) : that.context != null);

    }

    @Override
    public int hashCode() {
        int result = file != null ? file.hashCode() : 0;
        result = 31 * result + (method != null ? method.hashCode() : 0);
        result = 31 * result + (number != null ? number.hashCode() : 0);
        result = 31 * result + (context != null ? context.hashCode() : 0);
        return result;
    }
}
