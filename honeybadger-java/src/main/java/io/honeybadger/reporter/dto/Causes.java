package io.honeybadger.reporter.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.honeybadger.reporter.config.ConfigContext;

import java.io.Serializable;
import java.util.LinkedList;

/**
 * An ordered collection of chained exceptions.
 * @author <a href="https://github.com/dekobon">Elijah Zupancic</a>
 * @since 1.0.9
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@SuppressWarnings("JdkObsolete") // Reason: subtle interface change if we change LinkedList to ArrayList
public class Causes extends LinkedList<Cause> implements Serializable {
    private static final long serialVersionUID = -5359764114506595006L;

    private static final int MAX_CAUSES = 100;

    public Causes(final ConfigContext config, final Throwable rootError) {
        if (rootError == null) {
            throw new IllegalArgumentException("Error can't be null");
        }
        addCauses(config, rootError);
    }

    void addCauses(final ConfigContext config, final Throwable rootError) {
        if (rootError.getCause() == null) return;

        Throwable lastCause = null;
        Throwable nextCause = rootError.getCause();

        int iterations = 0;

        do {
            addFirst(new Cause(config, nextCause));
            ++iterations;
            lastCause = nextCause;
            nextCause = nextCause.getCause();
        } while (null != nextCause &&
                // If we are in a simple circular reference, stop.
                !lastCause.equals(nextCause) &&
                // Since we could have multi-class circular ref we just check
                // for too big of a cause trace
                iterations <= MAX_CAUSES);
    }
}
