package io.honeybadger.reporter.dto;

import java.io.Serializable;

/**
 * One single line on a backtrace.
 */
public class BacktraceElement implements Serializable {
    private static final long serialVersionUID = -4455225669072193184L;

    public final String number;
    public final String file;
    public final String method;

    public BacktraceElement(String number, String file, String method) {
        this.number = number;
        this.file = file;
        this.method = method;
    }

    public BacktraceElement(StackTraceElement element) {
        this.number = String.valueOf(element.getLineNumber());
        this.file = element.getFileName();
        this.method = formatMethod(element);
    }

    static String formatMethod(StackTraceElement element) {
        return String.format("%s.%s",
                element.getClassName(), element.getMethodName());
    }
}
