package io.honeybadger.reporter.dto;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * Details of the error being reported to the Honeybadger API.
 * @author <a href="https://github.com/dekobon">Elijah Zupancic</a>
 * @since 1.0.9
 */
public class ErrorDetails implements Serializable {
    private static final long serialVersionUID = -3055963787038629496L;

    @SerializedName("class")
    public final String className;
    public final String message;
    public final Set<String> tags;
    public final Backtrace backtrace;
    public final Causes causes;

    @SuppressWarnings("unchecked")
    public ErrorDetails(Throwable error) {

        this(error, (Set<String>)Collections.EMPTY_SET);
    }

    public ErrorDetails(Throwable error, Set<String> tags) {
        if (error == null) {
            throw new IllegalArgumentException("Error can't be null");
        }

        this.className = error.getClass().getName();
        this.message = error.getMessage();
        this.tags = tags;
        this.backtrace = new Backtrace(error);
        this.causes = new Causes(error);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ErrorDetails that = (ErrorDetails) o;

        if (className != null ? !className.equals(that.className) : that.className != null) return false;
        if (message != null ? !message.equals(that.message) : that.message != null) return false;
        if (tags != null ? !tags.equals(that.tags) : that.tags != null) return false;
        if (backtrace != null ? !backtrace.equals(that.backtrace) : that.backtrace != null) return false;
        return !(causes != null ? !causes.equals(that.causes) : that.causes != null);

    }

    @Override
    public int hashCode() {
        int result = className != null ? className.hashCode() : 0;
        result = 31 * result + (message != null ? message.hashCode() : 0);
        result = 31 * result + (tags != null ? tags.hashCode() : 0);
        result = 31 * result + (backtrace != null ? backtrace.hashCode() : 0);
        result = 31 * result + (causes != null ? causes.hashCode() : 0);
        return result;
    }
}
