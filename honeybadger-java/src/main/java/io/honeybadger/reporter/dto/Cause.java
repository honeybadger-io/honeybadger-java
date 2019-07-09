package io.honeybadger.reporter.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.honeybadger.reporter.config.ConfigContext;

import java.io.Serializable;
import java.util.Objects;

/**
 * This class represents a single exception in a serious of chained
 * exceptions.
 * @author <a href="https://github.com/dekobon">Elijah Zupancic</a>
 * @since 1.0.9
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Cause implements Serializable {
    private static final long serialVersionUID = -6876640270344752492L;

    @JsonProperty("class")
    private final String className;
    private final String message;
    private final Backtrace backtrace;

    public Cause(final ConfigContext config, final Throwable error) {
        this.className = error.getClass().getName();
        this.message = error.getMessage();
        this.backtrace = new Backtrace(config, error);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof Cause)) return false;
        Cause cause = (Cause) o;
        return Objects.equals(getClassName(), cause.getClassName()) &&
                Objects.equals(getMessage(), cause.getMessage()) &&
                Objects.equals(getBacktrace(), cause.getBacktrace());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getClassName(), getMessage(), getBacktrace());
    }

    public String getClassName() {
        return className;
    }

    public String getMessage() {
        return message;
    }

    public Backtrace getBacktrace() {
        return backtrace;
    }
}
