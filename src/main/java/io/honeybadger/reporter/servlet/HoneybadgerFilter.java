package io.honeybadger.reporter.servlet;

import io.honeybadger.reporter.FeedbackForm;
import io.honeybadger.reporter.NoticeReportResult;
import io.honeybadger.reporter.NoticeReporter;
import io.honeybadger.reporter.HoneybadgerReporter;
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
 * @author Elijah Zupancic
 * @since 1.0.4
 */
public class HoneybadgerFilter implements Filter {
    private NoticeReporter reporter;
    private FeedbackForm feedbackForm;
    private Properties properties = System.getProperties();

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        setSysPropFromFilterConfig(filterConfig, HONEYBADGER_URL_SYS_PROP_KEY);
        setSysPropFromFilterConfig(filterConfig, HONEYBADGER_API_KEY_SYS_PROP_KEY);
        setSysPropFromFilterConfig(filterConfig, HONEYBADGER_EXCLUDED_PROPS_SYS_PROP_KEY);
        setSysPropFromFilterConfig(filterConfig, HONEYBADGER_EXCLUDED_CLASSES_SYS_PROP_KEY);
        setSysPropFromFilterConfig(filterConfig, APPLICATION_PACKAGE_PROP_KEY);
        setSysPropFromFilterConfig(filterConfig, DISPLAY_FEEDBACK_FORM_KEY);
        setSysPropFromFilterConfig(filterConfig, FEEDBACK_FORM_TEMPLATE_PATH_KEY);

        reporter = new HoneybadgerReporter();
        feedbackForm = new FeedbackForm(feedbackFormTemplatePath());
    }

    static String feedbackFormTemplatePath() {
        String templatePath = System.getProperty(FEEDBACK_FORM_TEMPLATE_PATH_KEY);

        if (templatePath == null || templatePath.isEmpty()) return DEFAULT_FEEDBACK_FORM_TEMPLATE_PATH;

        return templatePath;
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
            if (!displayFeedbackForm()) {
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

    protected boolean displayFeedbackForm() {
        String enabled = System.getProperty(DISPLAY_FEEDBACK_FORM_KEY);

        if (enabled == null || enabled.isEmpty()) return true;

        if (enabled.equalsIgnoreCase("false") || enabled.equalsIgnoreCase("off")) {
            return false;
        }

        return true;
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

    /**
     * Sets a system property based on the servlet config.
     */
    private void setSysPropFromFilterConfig(FilterConfig filterConfig,
                                            String param) {
        final String val = filterConfig.getInitParameter(param);

        // Don't overwrite already set properties
        if (properties.getProperty(param) != null) return;

        if (val != null && !val.trim().equals("")) {
            properties.setProperty(param, val);
        }
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

    Properties getProperties() {
        return properties;
    }

    void setProperties(Properties properties) {
        this.properties = properties;
    }
}
