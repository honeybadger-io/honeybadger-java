package io.honeybadger.reporter;

import java.util.Scanner;

/**
 * Simple CLI utility that will allow you to post an error message to
 * Honeybadger.
 *
 * @author <a href="https://github.com/dekobon">Elijah Zupancic</a>
 * @since 1.0.0
 */
public class HoneybadgerCLI {
    public static void main(String[] argv) {
        Scanner in = new Scanner(System.in);

        System.out.print("What is your Honeybadger API key: ");
        System.setProperty(HoneybadgerReporter.HONEYBADGER_API_KEY_SYS_PROP_KEY,
                in.nextLine());
        System.out.print("\n");

        System.out.print("What message do you want to send: ");
        String message = in.nextLine();

        RuntimeException exception = new RuntimeException(message);
        NoticeReporter reporter = new HoneybadgerReporter();
        reporter.reportError(exception);
    }
}
