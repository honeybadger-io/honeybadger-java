package io.honeybadger.reporter.dto;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * Details of the error being reported to the Honeybadger API.
 */
public class ErrorDetails implements Serializable {
    private static final long serialVersionUID = -3055963787038629496L;

    @SerializedName("class")
    public final String className;
    public final String message;
    public final Set<String> tags;
    public final SourceDetails source;
    public final Backtrace backtrace;
    public final Causes causes;

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
        this.source = new SourceDetails(error);
        this.backtrace = new Backtrace(error);
        this.causes = new Causes(error);
    }
}
