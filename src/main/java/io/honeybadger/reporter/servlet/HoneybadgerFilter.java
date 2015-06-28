package io.honeybadger.reporter.servlet;

import io.honeybadger.reporter.HoneybadgerReporter;

import javax.servlet.*;
import java.io.IOException;

import static io.honeybadger.reporter.HoneybadgerReporter.*;

/**
 * Servlet filter that reports all unhandled servlet errors to Honeybadger.
 *
 * @author Elijah Zupancic
 * @since 1.0.4
 */
public class HoneybadgerFilter  implements Filter {
    private HoneybadgerReporter reporter;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        setSysPropFromfilterConfig(filterConfig, HONEYBADGER_URL_SYS_PROP_KEY);
        setSysPropFromfilterConfig(filterConfig, HONEYBADGER_API_KEY_SYS_PROP_KEY);
        setSysPropFromfilterConfig(filterConfig, HONEYBADGER_EXCLUDED_PROPS_SYS_PROP_KEY);
        setSysPropFromfilterConfig(filterConfig, HONEYBADGER_EXCLUDED_CLASSES_SYS_PROP_KEY);

        reporter = new HoneybadgerReporter();
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain)
            throws IOException, ServletException {
        try {
            chain.doFilter(request, response);
        } catch (Throwable e) {
            reporter.reportError(e, request);
            throw e;
        }
    }

    @Override
    public void destroy() {
        // do nothing
    }

    /**
     * Sets a system property based on the servlet config.
     */
    private void setSysPropFromfilterConfig(FilterConfig filterConfig,
                                            String param) {
        final String val = filterConfig.getInitParameter(param);

        // Don't overwrite already set properties
        if (System.getProperty(param) != null) return;

        if (val != null && !val.trim().equals("")) {
            System.setProperty(param, val);
        }
    }
}
