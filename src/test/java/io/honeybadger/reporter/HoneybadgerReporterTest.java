package io.honeybadger.reporter;

import com.google.common.collect.ImmutableMap;
import io.honeybadger.reporter.servlet.FakeHttpSession;
import org.apache.http.HttpHeaders;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static io.honeybadger.reporter.HoneybadgerReporter.HONEYBADGER_API_KEY_SYS_PROP_KEY;
import static io.honeybadger.reporter.HoneybadgerReporter.HONEYBADGER_EXCLUDED_CLASSES_SYS_PROP_KEY;
import static com.google.common.collect.ImmutableList.of;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class HoneybadgerReporterTest {
    private Logger logger = LoggerFactory.getLogger(getClass());
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
        HashMap<String, String> params = new HashMap<>();
        params.put("url", "http://foo.com");

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("http://www.foobar.com");
        when(request.getMethod()).thenReturn("GET");
        when(request.getServerName()).thenReturn("hostname.imaginary");
        when(request.getServerPort()).thenReturn(80);
        when(request.getContentType()).thenReturn("application/json; charset=UTF-8");

        Map<String, Object> sessionContents = ImmutableMap.of("session_key_1", (Object)"session_val_1");
        HttpSession session = new FakeHttpSession("session-id", sessionContents);
        when(request.getSession()).thenReturn(session);

        when(request.getHeaderNames()).thenReturn(
                EnumerationWrapper.of(
                        HttpHeaders.REFERER,
                        HttpHeaders.USER_AGENT,
                        HttpHeaders.ACCEPT));

        when(request.getHeader(HttpHeaders.REFERER)).thenReturn("Tester");
        when(request.getHeader(HttpHeaders.USER_AGENT)).thenReturn("User Agent");
        when(request.getHeader(HttpHeaders.ACCEPT)).thenReturn("application/json");

        Part part = mock(Part.class);
        when(part.getName()).thenReturn("testpart");
        when(part.toString()).thenReturn("value");

        when(request.getParts()).thenReturn(of(part));

        UUID id = reporter.reportError(t, request);

        logger.info("Error ID returned from Honeybadger is: {}", id);

        assertNotNull("Didn't send error correctly to Honeybadger API", id);

        logger.info("Created error with id: {}", id);
    }

    @Test
    public void willSuppressExcludedExceptionClasses() {
        final Exception error = new UnsupportedOperationException(
                "I should be suppressed");
        final UUID id = reporter.reportError(error);

        assertNull("A suppressed error was actually added", id);
    }
}
