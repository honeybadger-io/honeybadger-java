package io.honeybadger.reporter.spring;

import io.honeybadger.reporter.FeedbackForm;
import io.honeybadger.reporter.HoneybadgerReporter;
import io.honeybadger.reporter.NoticeReportResult;
import io.honeybadger.reporter.NoticeReporter;
import io.honeybadger.reporter.config.SpringConfigContext;
import org.apache.http.entity.ContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.handler.SimpleMappingExceptionResolver;

import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.*;

import static javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;

/**
 * Global exception handler for Spring MVC. This handler will use the
 * feedback form to render to users errors by default.
 *
 * @author <a href="https://github.com/dekobon">Elijah Zupancic</a>
 * @since 1.0.11
 */
@EnableWebMvc
@Component
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
public class HoneybadgerExceptionResolver
        extends SimpleMappingExceptionResolver
        implements Ordered {
    protected final SpringConfigContext context;
    protected final NoticeReporter reporter;
    protected final FeedbackForm feedbackForm;

    @Autowired
    public HoneybadgerExceptionResolver(SpringConfigContext context) {
        this.context = context;
        this.reporter = new HoneybadgerReporter(context);
        this.feedbackForm = new FeedbackForm(context);

        if (context.isFeedbackFormDisplayed()) {
            setDefaultErrorView(null);
            setDefaultStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
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
            throws IOException {
        String json = String.format("{ error_id : \"%s\" }", errorId);
        response.setContentType(ContentType.APPLICATION_JSON.getMimeType());
        response.getWriter().append(json);
    }

    protected String jsonErrorString(UUID errorId)
            throws IOException {
        return String.format("{ error_id : \"%s\" }", errorId);
    }

    @Override
    protected ModelAndView doResolveException(HttpServletRequest request,
                                              HttpServletResponse response,
                                              Object handler,
                                              Exception exception) {
        /* Use error handlers in the cases where they have been explicitly defined.
         * We don't use the default model view handler because we set it to null in the
         * initializer, so when there aren't handlers defined for the cases that we are
         * processing, we will proceed with our own implementation.  */
        ModelAndView modelAndView = super.doResolveException(request, response, handler, exception);
        if (modelAndView != null) return modelAndView;

        NoticeReportResult result = reporter.reportError(exception, request);

        if (!context.isFeedbackFormDisplayed()) {
            return null;
        }

        response.reset();

        // Output JSON if request only supports JSON
        if (acceptsOnlyJson(request)) {
            try {
                jsonError(result.getId(), response);
            } catch (IOException e) {
                // do nothing - we couldn't write to the client
            }
            return null;
        }

        // Otherwise, output feedback form

        response.setContentType(ContentType.TEXT_HTML.getMimeType());
        response.setStatus(SC_INTERNAL_SERVER_ERROR);

        Object errorId = result == null ? null : result.getId();

        try {
            feedbackForm.renderHtml(errorId, exception.getMessage(),
                    response.getWriter(), request.getLocale());
        } catch (IOException e) {
            // do nothing - we couldn't write to the client
        }

        return null;
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE; // make sure we are the default handler
    }

    /**
     * Renders an error feedback page to the client if HTML is considered an
     * accepted display format. Renders JSON if it is the only supported format.
     *
     * @param request HTTP request invoked that cause the error
     * @param exception exception representing the failure
     * @return Content displayed to the user representing the failure
     * @throws Throwable when the feedback form is disabled, we rethrow the exception
     */
    @ExceptionHandler(Throwable.class)
    public ResponseEntity<String> handleError(HttpServletRequest request,
            Throwable exception) throws Throwable {

        // Rethrow annotated exceptions or they will be processed here instead OR
        // throw if we have feedback form disabled
        if (AnnotationUtils.findAnnotation(exception.getClass(), ResponseStatus.class) != null) {
            throw exception;
        }

        NoticeReportResult result = reporter.reportError(exception, request);

        if (!context.isFeedbackFormDisplayed()) {
            throw exception;
        }

        if (acceptsOnlyJson(request)) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(jsonErrorString(result.getId()));
        }

        Writer writer = new StringWriter();
        Locale locale = request.getLocale();
        feedbackForm.renderHtml(result.getId(), exception.getMessage(),
                writer, locale);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .contentType(MediaType.TEXT_HTML)
                .body(writer.toString());
    }
}
