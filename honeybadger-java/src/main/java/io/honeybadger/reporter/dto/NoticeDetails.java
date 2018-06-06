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
    public NoticeDetails(final ConfigContext config, final Throwable error) {
        this(config, error, Collections.emptySet());
    }

    public NoticeDetails(final ConfigContext config, final Throwable error, final Set<String> tags) {
        this(config, error, tags, error.getMessage());
    }

    public NoticeDetails(final ConfigContext config, final Throwable error, final Set<String> tags,
                         final String message) {
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
    public boolean equals(final Object o) {
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
