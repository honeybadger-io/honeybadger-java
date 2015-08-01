package io.honeybadger.reporter.servlet;

import io.honeybadger.reporter.ErrorReporter;
import io.honeybadger.reporter.HoneybadgerReporter;

import javax.servlet.*;
import java.io.IOException;
import java.util.Properties;
import java.util.UUID;

import static io.honeybadger.reporter.HoneybadgerReporter.*;

/**
 * Servlet filter that reports all unhandled servlet errors to Honeybadger.
 *
 * @author Elijah Zupancic
 * @since 1.0.4
 */
public class HoneybadgerFilter implements Filter {
    private ErrorReporter reporter;
    private Properties properties = System.getProperties();

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        setSysPropFromfilterConfig(filterConfig, HONEYBADGER_URL_SYS_PROP_KEY);
        setSysPropFromfilterConfig(filterConfig, HONEYBADGER_API_KEY_SYS_PROP_KEY);
        setSysPropFromfilterConfig(filterConfig, HONEYBADGER_EXCLUDED_PROPS_SYS_PROP_KEY);
        setSysPropFromfilterConfig(filterConfig, HONEYBADGER_EXCLUDED_CLASSES_SYS_PROP_KEY);
        setSysPropFromfilterConfig(filterConfig, APPLICATION_PACKAGE_PROP_KEY);

        reporter = new HoneybadgerReporter();
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain)
            throws IOException, ServletException {
        try {
            chain.doFilter(request, response);
        } catch (Throwable e) {
            // TODO: Figure out how I can be displaayed to a user
            UUID id = reporter.reportError(e, request);
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
        if (properties.getProperty(param) != null) return;

        if (val != null && !val.trim().equals("")) {
            properties.setProperty(param, val);
        }
    }

    ErrorReporter getReporter() {
        return reporter;
    }

    void setReporter(ErrorReporter reporter) {
        this.reporter = reporter;
    }

    Properties getProperties() {
        return properties;
    }

    void setProperties(Properties properties) {
        this.properties = properties;
    }
}
