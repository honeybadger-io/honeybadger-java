package io.honeybadger.reporter.dto;

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

    public Backtrace(Throwable error) {
        if (error == null) {
            throw new IllegalArgumentException("Error must not be null");
        }

        addTrace(error);
    }

    void addTrace(Throwable error) {
        for (StackTraceElement trace : error.getStackTrace()) {
            add(new BacktraceElement(trace));
        }
    }
}
