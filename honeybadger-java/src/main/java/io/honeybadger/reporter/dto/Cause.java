package io.honeybadger.reporter.dto;

import com.google.gson.annotations.SerializedName;
import io.honeybadger.reporter.config.ConfigContext;

import java.io.Serializable;
import java.util.Objects;

/**
 * This class represents a single exception in a serious of chained
 * exceptions.
 * @author <a href="https://github.com/dekobon">Elijah Zupancic</a>
 * @since 1.0.9
 */
public class Cause implements Serializable {
    private static final long serialVersionUID = -6876640270344752492L;

    @SerializedName("class")
    public final String className;
    public final String message;
    public final Backtrace backtrace;

    public Cause(final ConfigContext config, final Throwable error) {
        this.className = error.getClass().getName();
        this.message = error.getMessage();
        this.backtrace = new Backtrace(config, error);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Cause cause = (Cause) o;
        return Objects.equals(className, cause.className) &&
                Objects.equals(message, cause.message) &&
                Objects.equals(backtrace, cause.backtrace);
    }

    @Override
    public int hashCode() {
        return Objects.hash(className, message, backtrace);
    }
}
