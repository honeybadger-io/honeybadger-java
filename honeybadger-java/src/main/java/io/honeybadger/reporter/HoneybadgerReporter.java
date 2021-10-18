package io.honeybadger.reporter;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.honeybadger.reporter.config.ConfigContext;
import io.honeybadger.reporter.config.SystemSettingsConfigContext;
import io.honeybadger.reporter.dto.HttpServletRequestFactory;
import io.honeybadger.reporter.dto.Notice;
import io.honeybadger.reporter.dto.NoticeDetails;
import io.honeybadger.reporter.dto.PlayHttpRequestFactory;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.apache.http.entity.ContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Reporter utility class that gives a simple interface for sending Java
 * {@link java.lang.Throwable} classes to the Honeybadger API.
 *
 * @author <a href="https://github.com/page1">page1</a>
 * @author <a href="https://github.com/dekobon">Elijah Zupancic</a>
 * @since 1.0.0
 */
public class HoneybadgerReporter implements NoticeReporter {
    private static Set<Class<?>> exceptionContextClasses = findExceptionContextClasses();

    private ConfigContext config;
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .setSerializationInclusion(JsonInclude.Include.NON_NULL);

    public HoneybadgerReporter() {
        this(new SystemSettingsConfigContext());
    }

    public HoneybadgerReporter(final ConfigContext config) {
        this.setConfig(config);

        if (config.getApiKey() == null) {
            throw new IllegalArgumentException("API key must be set");
        }

        if (config.getApiKey().isEmpty()) {
            throw new IllegalArgumentException("API key must not be empty");
        }

        if (config.getHoneybadgerUrl() == null) {
            throw new IllegalArgumentException("Honeybadger URL must be set");
        }
        if (config.getMaximumErrorReportingRetries() == null) {
            throw new IllegalArgumentException("Honeybadger property honeybadger.maximum_retry_attempts must be set");
        }
        if (config.getMaximumErrorReportingRetries() < 0) {
            throw new IllegalArgumentException("Honeybadger property honeybadger.maximum_retry_attempts must be an " +
                    "integer greater than or equal to 0");
        }
    }

    /**
     * Send any Java {@link java.lang.Throwable} to the Honeybadger error
     * reporting interface.
     *
     * @param error error to report
     * @return UUID of error created, if there was a problem null
     */
    @Override
    public NoticeReportResult reportError(final Throwable error) {
        return reportError(error, null, null, Collections.emptySet());
    }

    /**
     * Send any Java {@link java.lang.Throwable} to the Honeybadger error
     * reporting interface.
     *
     * Currently only {@link javax.servlet.http.HttpServletRequest} objects
     * are supported as request properties.
     *
     * @param error error to report
     * @param request Object to parse for request properties
     * @return UUID of error created, if there was a problem or ignored null
     */
    @Override
    public NoticeReportResult reportError(final Throwable error,
                                          final Object request) {
        return reportError(error, request, null, Collections.emptySet());
    }

    /**
     * Send any Java {@link java.lang.Throwable} to the Honeybadger error
     * reporting interface.
     *
     * Currently only {@link javax.servlet.http.HttpServletRequest} objects
     * are supported as request properties.
     *
     * @param error error to report
     * @param request Object to parse for request properties
     * @param message message to report instead of message associated with exception
     * @return UUID of error created, if there was a problem or ignored null
     */
    @Override
    public NoticeReportResult reportError(final Throwable error,
                                          final Object request,
                                          final String message) {
        return reportError(error, request, message, Collections.emptySet());
    }

    /**
     * Send any Java {@link java.lang.Throwable} to the Honeybadger error
     * reporting interface.
     *
     * Currently only {@link javax.servlet.http.HttpServletRequest} objects
     * are supported as request properties.
     *
     * @param error error to report
     * @param request Object to parse for request properties
     * @param message message to report instead of message associated with exception
     * @param tags tag values (duplicates will be removed)
     * @return UUID of error created, if there was a problem or ignored null
     */
    @Override
    public NoticeReportResult reportError(final Throwable error,
                                          final Object request,
                                          final String message,
                                          final Iterable<String> tags) {
        if (error == null) {
            return null;
        }

        final Set<String> tagsSet = aggregateTags(tags);

        if (request == null) {
            return submitError(error, null, message, tagsSet);
        }

        final io.honeybadger.reporter.dto.Request requestDetails;

        // CUSTOM USAGE OF REQUEST DTO
        if (request instanceof io.honeybadger.reporter.dto.Request) {
            logger.debug("Reporting using a request DTO");
            requestDetails = (io.honeybadger.reporter.dto.Request)request;

        // SERVLET REQUEST - ALSO USED BY SPRING
        } else if (supportsHttpServletRequest() && request instanceof javax.servlet.http.HttpServletRequest)  {
            logger.debug("Reporting from a servlet context");
            requestDetails =  HttpServletRequestFactory.create(getConfig(),
                    (javax.servlet.http.HttpServletRequest) request);

        // PLAY FRAMEWORK REQUEST
        } else if (supportsPlayHttpRequest() && request instanceof play.mvc.Http.Request) {
            logger.debug("Reporting from the Play Framework");
            requestDetails = PlayHttpRequestFactory.create(getConfig(),
                    (play.mvc.Http.Request)request);
        } else {
            logger.debug("No request object available");
            requestDetails = null;
        }

        return submitError(error, requestDetails, message, tagsSet);
    }

    @Override
    public ConfigContext getConfig() {
        return config;
    }

    /**
     * Processes an {@link Iterable} of Strings, discards invalid values and
     * aggregates all values into an ordered set.
     *
     * @param tags tag values
     * @return unique set of tags in the same order as input values
     */
    protected Set<String> aggregateTags(final Iterable<String> tags) {
        if (tags == null || tags == Collections.EMPTY_SET) {
            return Collections.emptySet();
        }

        final Iterator<String> itr = tags.iterator();

        if (!itr.hasNext()) {
            return Collections.emptySet();
        }

        final LinkedHashSet<String> tagHashSet = new LinkedHashSet<>();

        while (itr.hasNext()) {
            final String tag = itr.next();

            if (tag == null) {
                continue;
            }

            final String trimmed = tag.trim();

            if (!trimmed.isEmpty()) {
                tagHashSet.add(trimmed);
            }
        }

        return Collections.unmodifiableSet(tagHashSet);
    }

    @SuppressWarnings("LiteralClassName")
    protected boolean supportsHttpServletRequest() {
        try {
            Class.forName("javax.servlet.http.HttpServletRequest");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    @SuppressWarnings("LiteralClassName")
    protected boolean supportsPlayHttpRequest() {
        try {
            Class.forName("play.mvc.Http", false, this.getClass().getClassLoader());
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    protected NoticeReportResult submitError(final Throwable error,
                                             final io.honeybadger.reporter.dto.Request request,
                                             final String message,
                                             final Set<String> tags) {
        final String errorClassName = error.getClass().getName();
        if (errorClassName != null &&
                getConfig().getExcludedClasses().contains(errorClassName)) {
            return null;
        }

        final Notice notice = new Notice(getConfig());

        if (request != null) {
            final String reportedMessage;
            if (message != null && !message.isEmpty()) {
                reportedMessage = message;
            } else {
                reportedMessage = parseMessage(error);
            }

            NoticeDetails noticeDetails = new NoticeDetails(
                    getConfig(), error, tags, reportedMessage);
            notice.setRequest(request).setError(noticeDetails);
        } else {
            NoticeDetails noticeDetails = new NoticeDetails(getConfig(), error, tags);
            notice.setError(noticeDetails);
        }

        /* We may need to retry sending the JSON, so we temporarily keep it as a string.
           Future enhancement may convert this to a stream, but that's likely to require a
           bit more testing.
         */
        String json;
        try {
            json = OBJECT_MAPPER.writeValueAsString(notice);
        } catch (JsonProcessingException e) {
            logger.error("JSON Serialization of the Notice Failed.", e);
            logger.error("Original Error", error);
            return null;
        }

        for (int retries = 0; retries <= config.getMaximumErrorReportingRetries(); retries++) {
            try {
                HttpResponse response = sendToHoneybadger(json)
                        .returnResponse();
                int responseCode = response.getStatusLine().getStatusCode();

                if (responseCode != HttpStatus.SC_CREATED) {
                    logger.error("Honeybadger did not respond with the " +
                                    "correct code. Response was [{}]. Retries={}, Maximum={}",
                            responseCode, retries, config.getMaximumErrorReportingRetries());
                } else {
                    UUID id = parseErrorId(response);

                    return new NoticeReportResult(id, notice, error);
                }
            } catch (IOException e) {
                String msg = String.format("There was an error when trying " +
                                "to send the error to " +
                                "Honeybadger. Retries=%d, Maximum=%d", retries,
                        config.getMaximumErrorReportingRetries());
                logger.error(msg, e);
                logger.error("Original Error", error);
            }
        }

        return null;
    }

    private UUID parseErrorId(final HttpResponse response)
            throws IOException {
        try (InputStream in = response.getEntity().getContent();
             Reader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {

            JsonNode responseNode = OBJECT_MAPPER.readTree(reader);

            if (responseNode.has("id")) {
                return UUID.fromString(responseNode.get("id").textValue());
            } else {
                return null;
            }
        }
    }

    /**
     * Parses the exception message and strips out redundant context information
     * if we are already sending the information as part of the error context.
     *
     * @param throwable throwable to parse message from
     * @return string containing the throwable's error message
     */
    private static String parseMessage(final Throwable throwable) {
        if (exceptionClassHasContextedVariables(throwable.getClass())) {
            final String msg = throwable.getMessage();
            final int contextSeparatorPos = msg.indexOf("Exception Context:");

            if (contextSeparatorPos == -1) {
                return msg;
            }

            return msg.substring(0, contextSeparatorPos).trim();
        } else {
            return throwable.getMessage();
        }
    }

    /**
     * Send an error encoded in JSON to the Honeybadger API.
     *
     * @param jsonError Error JSON payload
     * @return Status code from the Honeybadger API
     * @throws IOException thrown when a network exception was encountered
     */
    protected Response sendToHoneybadger(final String jsonError) throws IOException {
        URI honeybadgerUrl = URI.create(
                String.format("%s/%s", getConfig().getHoneybadgerUrl(), "v1/notices"));
        Request request = buildRequest(honeybadgerUrl, jsonError);

        return request.execute();
    }

    /**
     * Builds a Apache HTTP Client request object configured for calling the
     * Honeybadger API.
     *
     * @param honeybadgerUrl Endpoint location
     * @param jsonError Error JSON payload
     * @return a configured request object
     */
    private Request buildRequest(final URI honeybadgerUrl, final String jsonError) {
        Request request = Request
               .Post(honeybadgerUrl)
               .addHeader("X-API-Key", getConfig().getApiKey())
               .addHeader("Accept", "application/json")
               .version(HttpVersion.HTTP_1_1)
               .bodyString(jsonError, ContentType.APPLICATION_JSON);

        if (System.getProperty("http.proxyHost") != null &&
            !System.getProperty("http.proxyHost").isEmpty()) {
            int port = Integer.parseInt(System.getProperty("http.proxyPort"));
            HttpHost proxy = new HttpHost(System.getProperty("http.proxyHost"),
                                          port);

            request.viaProxy(proxy);
        }

        if (getConfig().getSocketTimeout() != null) {
            request.socketTimeout(getConfig().getSocketTimeout());
        }

        if (getConfig().getConnectTimeout() != null) {
            request.connectTimeout(getConfig().getConnectTimeout());
        }

        return request;
    }

    /**
     * Tests to see if a given exception class has embedded context variables
     * like {@link org.apache.commons.lang3.exception.ContextedException}.
     *
     * @param clazz class to compare
     * @return true if a contexted exception, otherwise false
     */
    private static boolean exceptionClassHasContextedVariables(final Class<?> clazz) {
        if (exceptionContextClasses == null || exceptionContextClasses.isEmpty()) {
            return false;
        }

        for (Class<?> exceptionClass : exceptionContextClasses) {
            if (exceptionClass.isAssignableFrom(clazz)) {
                return true;
            }
        }

        return false;
    }

    /**
     * @return The Apache Commons Lang3 exception context class or null if not found
     */
    @SuppressWarnings("LiteralClassName")
    private static Set<Class<?>> findExceptionContextClasses() {
        final String[] classNames = new String[] {
                "org.apache.commons.lang3.exception.ExceptionContext"
        };

        final Set<Class<?>> classes = new LinkedHashSet<>(classNames.length);

        for (String className : classNames) {
            try {
                classes.add(Class.forName(className));
            } catch (ClassNotFoundException ignored) {
            }
        }

        return Collections.unmodifiableSet(classes);
    }

    protected void setConfig(final ConfigContext config) {
        this.config = config;
    }
}
