package io.honeybadger.reporter.servlet;

import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;

/**
 * Test-only {@link javax.servlet.FilterConfig} implementation
 *
 * @author <a href="https://github.com/dekobon">Elijah Zupancic</a>
 * @since 1.0.9
 */
public class FakeFilterConfig implements FilterConfig {
    private final Map<String, String> initParams;

    public FakeFilterConfig(Map<String, String> initParams) {
        if (initParams == null) throw new IllegalArgumentException(
                "parameters must not be null");

        this.initParams = initParams;
    }

    @Override
    public String getFilterName() {
        return "FakeFilter";
    }

    @Override
    public ServletContext getServletContext() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public String getInitParameter(String name) {
        return initParams.get(name);
    }

    @Override
    public Enumeration<String> getInitParameterNames() {
        return Collections.enumeration(initParams.keySet());
    }
}
