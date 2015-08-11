package io.honeybadger.reporter.servlet;

import com.google.common.collect.ImmutableMap;
import io.honeybadger.reporter.NoticeReporter;
import io.honeybadger.reporter.UnitTestExpectedException;
import org.junit.Test;
import org.mockito.Mockito;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.Properties;

import static io.honeybadger.reporter.HoneybadgerReporter.HONEYBADGER_API_KEY_SYS_PROP_KEY;
import static io.honeybadger.reporter.HoneybadgerReporter.HONEYBADGER_EXCLUDED_PROPS_SYS_PROP_KEY;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.internal.verification.VerificationModeFactory.times;

public class HoneybadgerFilterTest {

    /**
     * Generates a fake instance of a {@link HoneybadgerFilter} for testing.
     */
    private HoneybadgerFilter instance() throws ServletException {
        ImmutableMap.Builder<String, String> mapBuilder = ImmutableMap.builder();

        for (Map.Entry<Object, Object> entry : System.getProperties().entrySet()) {
            if (entry.getKey() == null) continue;

            mapBuilder.put(String.valueOf(entry.getKey()),
                    String.valueOf(entry.getValue()));
        }

        final FilterConfig config = new FakeFilterConfig(mapBuilder.build());
        final HoneybadgerFilter filter = new HoneybadgerFilter();
        filter.init(config);

        return filter;
    }

    /**
     * Generates a fake instance of a {@link HoneybadgerFilter} for testing.
     * @param reporter reporter instance to use for testing
     */
    private HoneybadgerFilter instance(NoticeReporter reporter)
            throws ServletException {
        if (reporter == null) {
            throw new IllegalArgumentException("Reporter must not be null");
        }

        final HoneybadgerFilter filter = new HoneybadgerFilter();
        filter.setReporter(reporter);

        return filter;
    }

    @Test
    public void canSetPropertiesFromFilter() throws ServletException {
        Map<String, String> filterParams = ImmutableMap.of(
                HONEYBADGER_API_KEY_SYS_PROP_KEY, "ffffffff",
                HONEYBADGER_EXCLUDED_PROPS_SYS_PROP_KEY, "doober.main"
        );

        final FilterConfig config = new FakeFilterConfig(filterParams);
        final HoneybadgerFilter filter = new HoneybadgerFilter();
        filter.setProperties(new Properties());

        filter.init(config);

        assertEquals("Filter configuration was not imported into properties",
                filterParams.get(HONEYBADGER_API_KEY_SYS_PROP_KEY),
                filter.getProperties().getProperty(HONEYBADGER_API_KEY_SYS_PROP_KEY));

        assertEquals("Filter configuration was not imported into properties",
                filterParams.get(HONEYBADGER_EXCLUDED_PROPS_SYS_PROP_KEY),
                filter.getProperties().getProperty(HONEYBADGER_EXCLUDED_PROPS_SYS_PROP_KEY));
    }

    @Test
    public void filterCanThrowAnError() throws Exception {
        NoticeReporter reporter = mock(NoticeReporter.class);
        HoneybadgerFilter filter = instance(reporter);

        FilterChain chain = mock(FilterChain.class);
        Exception exception = new UnitTestExpectedException("Servlet Filter Exception");
        Mockito.doThrow(exception)
                .when(chain).doFilter(any(ServletRequest.class),
                                      any(ServletResponse.class));

        ServletRequest request = mock(ServletRequest.class);
        ServletResponse response = mock(ServletResponse.class);

        boolean thrown = false;

        try {
            filter.doFilter(request, response, chain);
        } catch (UnitTestExpectedException e) {
            thrown = true;
        }

        assertTrue("Expected exception not thrown", thrown);

        verify(reporter, times(1)).reportError(exception, request);
    }

    @Test
    public void filterCanThrowAnErrorToTheHoneybadgerAPI() throws Exception {
        HoneybadgerFilter filter = instance();

        FilterChain chain = mock(FilterChain.class);
        Exception exception = new UnitTestExpectedException("Servlet Filter Exception");
        Mockito.doThrow(exception)
                .when(chain).doFilter(any(ServletRequest.class),
                                      any(ServletResponse.class));

        ServletRequest request = mock(HttpServletRequest.class);
        ServletResponse response = mock(HttpServletResponse.class);

        boolean thrown = false;

        try {
            filter.doFilter(request, response, chain);
        } catch (UnitTestExpectedException e) {
            thrown = true;
        }

        assertTrue("Expected exception not thrown", thrown);
    }
}
