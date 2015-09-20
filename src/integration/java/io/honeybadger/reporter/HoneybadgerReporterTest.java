package io.honeybadger.reporter;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.honeybadger.loader.HoneybadgerNoticeLoader;
import io.honeybadger.reporter.config.ConfigContext;
import io.honeybadger.reporter.config.SystemSettingsConfigContext;
import io.honeybadger.reporter.dto.CgiData;
import io.honeybadger.reporter.dto.Notice;
import io.honeybadger.reporter.dto.Request;
import io.honeybadger.reporter.servlet.FakeHttpServletRequest;
import org.apache.http.HttpHeaders;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.*;

public class HoneybadgerReporterTest {
    private Logger logger = LoggerFactory.getLogger(getClass());
    private final HoneybadgerNoticeLoader loader;
    private final NoticeReporter reporter;
    private ConfigContext config = new SystemSettingsConfigContext();

    public HoneybadgerReporterTest() {
        if (this.config.getApiKey() == null) {
            throw new IllegalArgumentException("API key must be specified");
        }

        this.config.getExcludedClasses().add(UnsupportedOperationException.class.getName());
        this.config.getExcludedClasses().add(IllegalArgumentException.class.getName());

        this.loader = new HoneybadgerNoticeLoader(this.config);
        this.reporter = new HoneybadgerReporter(this.config);
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

        NoticeReportResult result = reporter.reportError(t, request);
        assertNotNull("Result of error report should never be null", result);

        UUID id = result.getId();

        logger.info("Error ID returned from Honeybadger is: {}", id);

        assertNotNull("Didn't send error correctly to Honeybadger API", id);

        logger.info("Created error with id: {}", id);

        // Wait for the Honeybadger API to process the error
        Thread.sleep(10000);
        Notice error = loader.findErrorDetails(id);
        assertReportedErrorIsSame(result.getNotice(), error);
    }

    @Test
    public void willSuppressExcludedExceptionClasses() throws Exception {
        final Exception error = new UnsupportedOperationException(
                "I should be suppressed");
        final NoticeReportResult result = reporter.reportError(error);

        assertNull("A suppressed error was actually added", result);
    }

    static void assertReportedErrorIsSame(Notice expected,
                                          Notice actual) {
        if (!expected.getDetails().equals(actual.getDetails())) {
            fail(String.format("Details were not equal.\n" +
                    "Expected: %s\n" +
                    "Actual:   %s",
                    expected.getDetails(), actual.getDetails()));
        }

        assertEquals(expected.getNotifier(), actual.getNotifier());
        assertEquals(expected.getServer(), actual.getServer());

        Request expectedRequest = expected.getRequest();
        Request actualRequest = actual.getRequest();

        assertEquals(expectedRequest.context, actualRequest.context);
        assertEquals(expectedRequest.params, actualRequest.params);
        assertEquals(expectedRequest.session, actualRequest.session);

        CgiData expectedCgi = expectedRequest.cgi_data;
        CgiData actualCgi = actualRequest.cgi_data;

        assertEquals(expectedCgi.getAsInteger(HttpHeaders.CONTENT_LENGTH.toUpperCase()),
                     actualCgi.getAsInteger(HttpHeaders.CONTENT_LENGTH.toUpperCase()));

        assertEquals(expectedCgi.getAsInteger("SERVER_PORT"),
                actualCgi.getAsInteger("SERVER_PORT"));

        assertEquals(expectedCgi.getAsInteger("REMOTE_PORT"),
                actualCgi.getAsInteger("REMOTE_PORT"));

        assertEquals(expectedCgi.get(HttpHeaders.CONTENT_TYPE.toUpperCase()),
                     actualCgi.get(HttpHeaders.CONTENT_TYPE.toUpperCase()));

        assertEquals(expectedCgi.get(HttpHeaders.ACCEPT.toUpperCase()),
                actualCgi.get(HttpHeaders.ACCEPT.toUpperCase()));

        assertEquals(expectedCgi.get("REQUEST_METHOD"),
                actualCgi.get("REQUEST_METHOD"));

        assertEquals(expectedCgi.get(HttpHeaders.USER_AGENT.toUpperCase()),
                actualCgi.get(HttpHeaders.USER_AGENT.toUpperCase()));

        assertEquals(expectedCgi.get("HTTP_COOKIE"),
                actualCgi.get("HTTP_COOKIE"));

        assertEquals(expectedCgi.get("SERVER_NAME"),
                actualCgi.get("SERVER_NAME"));

//        Note: We are waiting on an API that returns everything in order
//              for us to test this properly
//        ErrorDetails expectedError = expected.getError();
//        ErrorDetails actualError = actual.getError();
//
//        assertEquals(expectedError, actualError);
    }
}
