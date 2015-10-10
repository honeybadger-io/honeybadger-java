package io.honeybadger.util;

import java.util.Collection;

/**
 * Commonly shared Collection utilities.
 *
 * @author <a href="https://github.com/dekobon">Elijah Zupancic</a>
 * @since 1.0.11
 */
public class HBCollectionUtils {
    /**
     * Checks a collection to see if it is null or empty.
     * @param collection Collection to check
     * @return true if null or empty
     */
    public static boolean isPresent(Collection<?> collection) {
        return collection != null && !collection.isEmpty();
    }
}
