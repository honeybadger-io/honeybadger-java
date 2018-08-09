package io.honeybadger.reporter.spring;

import io.honeybadger.reporter.FeedbackForm;
import io.honeybadger.reporter.HoneybadgerReporter;
import io.honeybadger.reporter.NoticeReportResult;
import io.honeybadger.reporter.NoticeReporter;
import io.honeybadger.reporter.config.SpringConfigContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import static org.springframework.http.MediaType.*;

/**
 * Spring Framework web endpoint exception handler class. This class catches
 * unhandled exceptions that occurred when processing web requests. For handling
 * errors outside of the scope of of web requests, you will need to load
 * {@link io.honeybadger.reporter.HoneybadgerUncaughtExceptionHandler}.
 */
@ControllerAdvice
public class HoneybadgerSpringExceptionHandler {
    private final SpringConfigContext context;
    private final NoticeReporter reporter;
    private final FeedbackForm feedbackForm;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    public HoneybadgerSpringExceptionHandler(final SpringConfigContext context) {
        this.context = context;
        this.reporter = new HoneybadgerReporter(context);
        this.feedbackForm = new FeedbackForm(context);
    }

    protected boolean acceptsOnlyJson(final HttpServletRequest request) {
        Enumeration<String> enumeration = request.getHeaders("Accept");
        if (enumeration == null) return false;
        if (!enumeration.hasMoreElements()) return false;

        List<String> accepts = Collections.list(enumeration);

        if (accepts.size() == 1) {
            return accepts.get(0).equals(APPLICATION_JSON.toString());
        } else {
            return false;
        }
    }

    protected String jsonErrorString(final UUID errorId)
            throws IOException {
        return String.format("{ error_id : \"%s\" }", errorId);
    }

    @ExceptionHandler(value = Throwable.class)
    public ResponseEntity<String> defaultErrorHandler(final HttpServletRequest request,
                                            final Exception exception) throws Exception {

        // Rethrow annotated exceptions or they will be processed here instead OR
        // throw if we have feedback form disabled
        if (AnnotationUtils.findAnnotation(exception.getClass(), ResponseStatus.class) != null) {
            throw exception;
        }

        NoticeReportResult result = getReporter().reportError(exception, request);

        if (logger.isErrorEnabled()) {
            String msg = String.format("Internal server error [honeybadger-id: %s]",
                    result.getId());
            logger.error(msg, exception);
        }

        if (!getContext().isFeedbackFormDisplayed()) {
            String msg = "Internal server error";
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(TEXT_PLAIN)
                    .body(msg);
        }

        if (acceptsOnlyJson(request)) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(APPLICATION_JSON)
                    .body(jsonErrorString(result.getId()));
        }

        Writer writer = new StringWriter();
        Locale locale = request.getLocale();
        getFeedbackForm().renderHtml(result.getId(), exception.getMessage(),
                writer, locale);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .contentType(TEXT_HTML)
                .body(writer.toString());
    }

    protected SpringConfigContext getContext() {
        return context;
    }

    protected NoticeReporter getReporter() {
        return reporter;
    }

    protected FeedbackForm getFeedbackForm() {
        return feedbackForm;
    }
}
