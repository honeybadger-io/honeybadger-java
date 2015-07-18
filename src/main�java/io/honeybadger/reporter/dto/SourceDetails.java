package io.honeybadger.reporter.dto;

import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.LinkedHashMap;
import java.util.Scanner;

/**
 * Class representing the source of an exception.
 */
public class SourceDetails extends LinkedHashMap<String, String>
        implements Serializable {
    private static final long serialVersionUID = -3409445059430989705L;

    public SourceDetails(Throwable error) {
        if (error == null) {
            throw new IllegalArgumentException("Error must not be null");
        }

        appendLines(error);
    }

    /**
     * Adds lines to the backing map of this class.
     * @param error exception to parse
     */
    void appendLines(Throwable error) {
        String stack = stacktraceAsString(error);
        Scanner scanner = new Scanner(stack);

        int lineNo = 0;

        while (scanner.hasNext()) {
            final String line = String.valueOf(++lineNo);
            final String text = scanner.nextLine();

            put(line, text);
        }
    }

    /**
     * Renders an exception's stacktrace as a string.
     * @param error exception to parse
     * @return Java style stacktrace
     */
    String stacktraceAsString(Throwable error) {
        final StringWriter sw = new StringWriter();
        final PrintWriter pw = new PrintWriter(sw, true);
        error.printStackTrace(pw);

        final String trace = sw.getBuffer().toString();

        return trace.replace("\t", "  ");
    }
}
