package io.honeybadger.reporter.dto;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.honeybadger.reporter.config.ConfigContext;

import java.io.Serializable;
import java.util.Objects;

/**
 * One single line on a backtrace.
 * @author <a href="https://github.com/dekobon">Elijah Zupancic</a>
 * @since 1.0.9
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BacktraceElement implements Serializable {
    private static final long serialVersionUID = -4455225669072193184L;

    /**
     * Enum representing all of the valid context values on the Honeybadger API.
     */
    enum Context {
        /** Backtrace not-belonging to the calling application. **/
        ALL("all"),
        /** Backtrace belonging to the calling application. **/
        APP("app");

        private final String name;

        Context(final String name) {
            this.name = name;
        }

        String getName() {
            return this.name;
        }
    }

    @JacksonInject("config")
    private final ConfigContext config;

    public String getFile() {
        return file;
    }

    public String getMethod() {
        return method;
    }

    public String getNumber() {
        return number;
    }

    public String getContext() {
        return context;
    }

    private final String file;
    private final String method;
    private final String number;
    private final String context;

    @JsonCreator
    public BacktraceElement(@JacksonInject("config") final ConfigContext config,
                            @JsonProperty("number") final String number,
                            @JsonProperty("file") final String file,
                            @JsonProperty("method") final String method) {
        this.config = config;
        this.number = number;
        this.file = file;
        this.method = method;
        this.context = calculateContext(method).getName();
    }

    public BacktraceElement(final ConfigContext config, final StackTraceElement element) {
        this.config = config;
        this.number = String.valueOf(element.getLineNumber());
        this.file = String.valueOf(element.getFileName());
        this.method = formatMethod(element);
        this.context = calculateContext(method).getName();
    }

    static String formatMethod(final StackTraceElement element) {
        return String.format("%s.%s",
                element.getClassName(), element.getMethodName());
    }

    Context calculateContext(final String methodName) {
        final String appPackage = config.getApplicationPackage();
        final Context methodContext;

        if (appPackage == null || appPackage.isEmpty()) {
            methodContext = Context.ALL;
        } else if (methodName == null || methodName.isEmpty()) {
            methodContext = Context.ALL;
        } else if (methodName.startsWith(appPackage)) {
            methodContext = Context.APP;
        } else {
            methodContext = Context.ALL;
        }

        return methodContext;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof BacktraceElement)) return false;
        BacktraceElement that = (BacktraceElement) o;
        return Objects.equals(config, that.config) &&
                Objects.equals(file, that.file) &&
                Objects.equals(method, that.method) &&
                Objects.equals(number, that.number) &&
                Objects.equals(context, that.context);
    }

    @Override
    public int hashCode() {
        return Objects.hash(config, file, method, number, context);
    }
}
