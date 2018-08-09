package io.honeybadger.reporter;

import io.honeybadger.reporter.config.StandardConfigContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Simple CLI utility that will allow you to post an error message to
 * Honeybadger.
 *
 * @author <a href="https://github.com/dekobon">Elijah Zupancic</a>
 * @since 1.0.0
 */
public final class HoneybadgerCLI {
    private HoneybadgerCLI() { }

    @SuppressWarnings("DefaultCharset")
    public static void main(final String[] argv) {
        Scanner in = new Scanner(System.in);

        System.out.print("What is your Honeybadger API key: ");

        StandardConfigContext config = new StandardConfigContext();
        config.setApiKey(in.nextLine());
        System.out.print("\n");

        System.out.print("What message do you want to send: ");
        String message = in.nextLine();

        RuntimeException exception = new RuntimeException(message);
        NoticeReporter reporter = new HoneybadgerReporter(config);
        reporter.reportError(exception);

        io.honeybadger.reporter.dto.Context context = new io.honeybadger.reporter.dto.Context();
        context.put("context1", "data1");
        context.setUsername("bob");
        io.honeybadger.reporter.dto.Request request = new io.honeybadger.reporter.dto.Request(context, null, null, null, null);

        List<String> tags = new ArrayList<String>();
        tags.add("asdf");
        tags.add("baoo");
        reporter.reportError(exception, request, "asefsfa", tags);
    }
}
