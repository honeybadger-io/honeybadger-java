package io.honeybadger.reporter.dto;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * This class represents a single exception in a serious of chained
 * exceptions.
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
}
