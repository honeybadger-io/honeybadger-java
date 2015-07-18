package io.honeybadger.reporter.dto;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * An ordered collection of chained exceptions.
 */
public class Causes extends ArrayList<Cause> implements Serializable {
    private static final long serialVersionUID = -5359764114506595006L;

    private static final int MAX_CAUSES = 100;

    public Causes(Throwable rootError) {
        if (rootError == null) {
            throw new IllegalArgumentException("Error can't be null");
        }

        addCauses(rootError);
    }

    void addCauses(Throwable rootError) {
        if (rootError.getCause() == null) return;

        Throwable lastCause = null;
        Throwable nextCause = rootError.getCause();

        int iterations = 0;

        do {
            // If we are in a simple circular reference, exit
            if (lastCause != null && lastCause.equals(nextCause)) break;

            add(new Cause(nextCause));

            // Since we could have multi-class circular ref we just check
            // for too big of a cause trace
            if (++iterations > MAX_CAUSES) break;
            lastCause = nextCause;
        } while ((nextCause = nextCause.getCause()) != null);
    }
}
