package io.honeybadger.reporter.dto;

import com.google.gson.annotations.SerializedName;
import io.honeybadger.reporter.config.ConfigContext;

import java.io.Serializable;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;

/**
 * Details of the error being reported to the Honeybadger API.
 * @author <a href="https://github.com/dekobon">Elijah Zupancic</a>
 * @since 1.0.9
 */
public class NoticeDetails implements Serializable {
    private static final long serialVersionUID = -3055963787038629496L;

    @SerializedName("class")
    public final String className;
    public final String message;
    public final Set<String> tags;
    public final Backtrace backtrace;
    public final Causes causes;

    @SuppressWarnings("unchecked")
    public NoticeDetails(ConfigContext config, Throwable error) {
        this(config, error, Collections.emptySet());
    }

    public NoticeDetails(ConfigContext config, Throwable error, Set<String> tags) {
        this(config, error, tags, error.getMessage());
    }

    public NoticeDetails(ConfigContext config, Throwable error, Set<String> tags,
                         String message) {
        if (error == null) {
            throw new IllegalArgumentException("Error can't be null");
        }

        this.className = error.getClass().getName();
        this.message = message;
        this.tags = tags;
        this.backtrace = new Backtrace(config, error);
        this.causes = new Causes(config, error);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NoticeDetails that = (NoticeDetails) o;
        return Objects.equals(className, that.className) &&
                Objects.equals(message, that.message) &&
                Objects.equals(tags, that.tags) &&
                Objects.equals(backtrace, that.backtrace) &&
                Objects.equals(causes, that.causes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(className, message, tags, backtrace, causes);
    }
}
