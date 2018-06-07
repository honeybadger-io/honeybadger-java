package io.honeybadger.reporter.dto;

import io.honeybadger.reporter.config.ConfigContext;

import java.io.Serializable;
import java.util.LinkedList;

/**
 * An ordered collection of chained exceptions.
 * @author <a href="https://github.com/dekobon">Elijah Zupancic</a>
 * @since 1.0.9
 */
@SuppressWarnings("JdkObsolete")
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
            // If we are in a simple circular reference, exit
            if (lastCause != null && lastCause.equals(nextCause)) break;

            addFirst(new Cause(config, nextCause));

            // Since we could have multi-class circular ref we just check
            // for too big of a cause trace
            if (++iterations > MAX_CAUSES) break;
            lastCause = nextCause;
        } while (null != (nextCause = nextCause.getCause())); //checkstyle ignore
    }
}
