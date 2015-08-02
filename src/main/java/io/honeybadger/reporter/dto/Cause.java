package io.honeybadger.reporter.dto;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

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

    public Cause(Throwable error) {
        this.className = error.getClass().getName();
        this.message = error.getMessage();
        this.backtrace = new Backtrace(error);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Cause cause = (Cause) o;

        if (className != null ? !className.equals(cause.className) : cause.className != null) return false;
        if (message != null ? !message.equals(cause.message) : cause.message != null) return false;
        return !(backtrace != null ? !backtrace.equals(cause.backtrace) : cause.backtrace != null);

    }

    @Override
    public int hashCode() {
        int result = className != null ? className.hashCode() : 0;
        result = 31 * result + (message != null ? message.hashCode() : 0);
        result = 31 * result + (backtrace != null ? backtrace.hashCode() : 0);
        return result;
    }
}
