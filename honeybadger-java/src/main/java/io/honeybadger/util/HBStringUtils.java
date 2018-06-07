package io.honeybadger.util;

import java.util.Arrays;

/**
 * Commonly shared String utilities.
 *
 * @author <a href="https://github.com/dekobon">Elijah Zupancic</a>
 * @since 1.0.11
 */
public final class HBStringUtils {
    private HBStringUtils() { }

    /**
     * Removes a single character from the end of a string if it matches.
     * @param input String to remove from, if null returns null
     * @param c character to match
     * @return Original string minus matched character
     */
    public static String stripTrailingChar(final String input, final char c) {
        if (input == null) return null;
        if (input.isEmpty()) return input;

        char[] charArray = input.toCharArray();

        if (charArray[charArray.length - 1] == c) {
            return new String(Arrays.copyOf(charArray, charArray.length - 1));
        } else {
            return input;
        }
    }

    /**
     * Checks a string to see if it is null or empty.
     * @param string String to check
     * @return true if null or empty
     */
    public static boolean isPresent(final String string) {
        return string != null && !string.isEmpty();
    }
}
