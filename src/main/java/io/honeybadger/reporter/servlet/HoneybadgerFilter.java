package io.honeybadger.reporter.servlet;

import io.honeybadger.reporter.FeedbackForm;
import io.honeybadger.reporter.NoticeReportResult;
import io.honeybadger.reporter.NoticeReporter;
import io.honeybadger.reporter.HoneybadgerReporter;
import io.honeybadger.reporter.config.ConfigContext;
import io.honeybadger.reporter.config.ServletFilterConfigContext;
import io.honeybadger.reporter.config.SystemSettingsConfigContext;
import jdk.nashorn.internal.runtime.regexp.joni.Config;
import org.apache.http.entity.ContentType;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

import static io.honeybadger.reporter.HoneybadgerReporter.*;
import static javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;

/**
 * Servlet filter that reports all unhandled servlet errors to Honeybadger.
 *
 * @author <a href="https://github.com/dekobon">Elijah Zupancic</a>
 * @since 1.0.4
 */
public class HoneybadgerFilter implements Filter {
    private ConfigContext config;
    private NoticeReporter reporter;
    private FeedbackForm feedbackForm;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        ConfigContext filterContext = new ServletFilterConfigContext(filterConfig);
        ConfigContext config = new SystemSettingsConfigContext(filterContext);
        this.config = config;
        this.reporter = new HoneybadgerReporter(config);
        this.feedbackForm = new FeedbackForm(config);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain)
            throws IOException, ServletException {
        try {
            chain.doFilter(request, response);
        } catch (Throwable e) {
            NoticeReportResult result = reporter.reportError(e, request);

            // Don't render the feedback form and just throw the error
            if (config.isFeedbackFormDisplayed() != null && !config.isFeedbackFormDisplayed()) {
                throw e;
            }

            response.reset();

            // Output JSON if request only supports JSON
            if (request instanceof HttpServletRequest) {
                HttpServletRequest httpServletRequest = (HttpServletRequest)request;

                if (acceptsOnlyJson(httpServletRequest)) {
                    jsonError(result.getId(), response);
                    return;
                }
            }

            // Otherwise, output feedback form

            response.setContentType(ContentType.TEXT_HTML.getMimeType());

            if (response instanceof HttpServletResponse) {
                HttpServletResponse httpServletResponse = (HttpServletResponse)response;
                httpServletResponse.setStatus(SC_INTERNAL_SERVER_ERROR);
            }

            Object errorId = result == null ? null : result.getId();

            feedbackForm.renderHtml(errorId, response.getWriter(),
                    request.getLocale());
        }
    }

    protected boolean acceptsOnlyJson(HttpServletRequest request) {
        Enumeration<String> enumeration = request.getHeaders("Accept");
        if (enumeration == null) return false;
        if (!enumeration.hasMoreElements()) return false;

        List<String> accepts = Collections.list(enumeration);

        if (accepts.size() == 1) {
            return accepts.get(0).equals(ContentType.APPLICATION_JSON.getMimeType());
        } else {
            return false;
        }
    }

    protected void jsonError(UUID errorId, ServletResponse response)
            throws IOException{
        String json = String.format("{ error_id : \"%s\" }", errorId);
        response.setContentType(ContentType.APPLICATION_JSON.getMimeType());
        response.getWriter().append(json);
    }

    @Override
    public void destroy() {
        // do nothing
    }

    NoticeReporter getReporter() {
        return reporter;
    }

    void setReporter(NoticeReporter reporter) {
        this.reporter = reporter;
    }

    public void setFeedbackForm(FeedbackForm feedbackForm) {
        this.feedbackForm = feedbackForm;
    }

    ConfigContext getConfig() {
        return this.config;
    }
}
