package io.honeybadger.reporter;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.honeybadger.loader.HoneybadgerErrorLoader;
import io.honeybadger.reporter.dto.ReportedError;
import io.honeybadger.reporter.servlet.FakeHttpServletRequest;
import org.apache.http.HttpHeaders;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

import static io.honeybadger.reporter.HoneybadgerReporter.HONEYBADGER_API_KEY_SYS_PROP_KEY;
import static io.honeybadger.reporter.HoneybadgerReporter.HONEYBADGER_EXCLUDED_CLASSES_SYS_PROP_KEY;
import static com.google.common.collect.ImmutableList.of;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;

public class HoneybadgerReporterTest {
    private Logger logger = LoggerFactory.getLogger(getClass());
    private final HoneybadgerErrorLoader loader = new HoneybadgerErrorLoader();
    private final ErrorReporter reporter;

    public HoneybadgerReporterTest() {
        if (System.getProperty(HONEYBADGER_API_KEY_SYS_PROP_KEY) == null) {
            throw new IllegalArgumentException(HONEYBADGER_API_KEY_SYS_PROP_KEY +
            " system property must be specified");
        }

        System.setProperty(HONEYBADGER_EXCLUDED_CLASSES_SYS_PROP_KEY,
                String.format("%s,%s",
                    UnsupportedOperationException.class.getName(),
                    IllegalArgumentException.class.getName()));

        reporter = new HoneybadgerReporter();
    }

    @Test
    public void willReportErrorWithRequest() throws Exception {
        MDC.put("testValue", "something");

        Throwable cause = new RuntimeException("I'm the cause");
        Throwable t = new UnitTestExpectedException("Test exception " +
                System.currentTimeMillis(), cause);

        ArrayList<String> cookies = new ArrayList<>(ImmutableList.of(
                "theme=light",
                "sessionToken=abc123; Expires=Wed, 09 Jun 2021 10:18:14 GMT",
                "multi-value=true; lastItem=true"));

        Map<String, ? extends List<String>> headers = ImmutableMap.of(
                HttpHeaders.REFERER, ImmutableList.of("Tester"),
                HttpHeaders.USER_AGENT, ImmutableList.of("User Agent"),
                HttpHeaders.ACCEPT, ImmutableList.of("text/html"),
                "Set-Cookie", cookies
        );

        HttpServletRequest request = new FakeHttpServletRequest(headers);

        UUID id = reporter.reportError(t, request);

        logger.info("Error ID returned from Honeybadger is: {}", id);

        assertNotNull("Didn't send error correctly to Honeybadger API", id);

        logger.info("Created error with id: {}", id);

        // Wait for the Honeybadger API to process the error
        Thread.sleep(3000);

        ReportedError error = loader.findErrorDetails(id);
        System.out.println(error);
    }

    @Test
    public void willSuppressExcludedExceptionClasses() {
        final Exception error = new UnsupportedOperationException(
                "I should be suppressed");
        final UUID id = reporter.reportError(error);

        assertNull("A suppressed error was actually added", id);
    }
}
