package io.honeybadger.reporter.dto;

import io.honeybadger.reporter.config.ConfigContext;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Class representing an ordered collection of back trace elements.
 * @author <a href="https://github.com/dekobon">Elijah Zupancic</a>
 * @since 1.0.9
 */
public class Backtrace extends ArrayList<BacktraceElement>
        implements Serializable {
    private static final long serialVersionUID = 5788866962863555294L;

    private final ConfigContext config;

    public Backtrace(ConfigContext config, Throwable error) {
        this.config = config;

        if (error == null) {
            throw new IllegalArgumentException("Error must not be null");
        }

        addTrace(error);
    }

    void addTrace(Throwable error) {
        for (StackTraceElement trace : error.getStackTrace()) {
            add(new BacktraceElement(config, trace));
        }
    }
}
