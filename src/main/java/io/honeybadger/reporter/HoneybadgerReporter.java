package io.honeybadger.reporter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.honeybadger.reporter.config.ConfigContext;
import io.honeybadger.reporter.config.SystemSettingsConfigContext;
import io.honeybadger.reporter.dto.HttpServletRequestFactory;
import io.honeybadger.reporter.dto.Notice;
import io.honeybadger.reporter.dto.NoticeDetails;
import io.honeybadger.reporter.dto.PlayHttpRequestFactory;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
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
import java.util.*;

/**
 * Reporter utility class that gives a simple interface for sending Java
 * {@link java.lang.Throwable} classes to the Honeybadger API.
 *
 * @author <a href="https://github.com/page1">page1</a>
 * @author <a href="https://github.com/dekobon">Elijah Zupancic</a>
 * @since 1.0.0
 */
public class HoneybadgerReporter implements NoticeReporter {
    protected ConfigContext config;
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final Gson gson = new GsonBuilder()
            .setExclusionStrategies(new HoneybadgerExclusionStrategy())
            .create();

    public HoneybadgerReporter() {
        this(new SystemSettingsConfigContext());
    }

    public HoneybadgerReporter(ConfigContext config) {
        this.config = config;

        if (config.getApiKey() == null) {
            throw new IllegalArgumentException("API key must be set");
        }

        if (config.getApiKey().isEmpty()) {
            throw new IllegalArgumentException("API key must not be empty");
        }

        if (config.getHoneybadgerUrl() == null) {
            throw new IllegalArgumentException("Honeybadger URL must be set");
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
    public NoticeReportResult reportError(Throwable error) {
        return submitError(error, null, null);
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
    public NoticeReportResult reportError(Throwable error, Object request) {
        return reportError(error, request, new HashSet<String>());
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
     * @param tags message tags
     * @return UUID of error created, if there was a problem or ignored null
     */
    @Override
    public NoticeReportResult reportError(Throwable error, Object request, Set<String> tags) {
        if (error == null) { return null; }
        if (request == null) { return submitError(error, null, tags); }

        final io.honeybadger.reporter.dto.Request requestDetails;

        // CUSTOM USAGE OF REQUEST DTO
        if (request instanceof io.honeybadger.reporter.dto.Request) {
            logger.debug("Reporting using a request DTO");
            requestDetails = (io.honeybadger.reporter.dto.Request)request;

        // SERVLET REQUEST - ALSO USED BY SPRING
        } else if (supportsHttpServletRequest() && request instanceof javax.servlet.http.HttpServletRequest)  {
            logger.debug("Reporting from a servlet context");
            requestDetails =  HttpServletRequestFactory.create(config,
                    (javax.servlet.http.HttpServletRequest) request);

        // PLAY FRAMEWORK REQUEST
        } else if (supportsPlayHttpRequest() && request instanceof play.mvc.Http.Request) {
            logger.debug("Reporting from the Play Framework");
            requestDetails = PlayHttpRequestFactory.create(config,
                    (play.mvc.Http.Request)request);
        } else {
            logger.debug("No request object available");
            requestDetails = null;
        }

        return submitError(error, requestDetails, tags);
    }

    protected boolean supportsHttpServletRequest() {
        try {
            Class.forName("javax.servlet.http.HttpServletRequest");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    protected boolean supportsPlayHttpRequest() {
        try {
            Class.forName("play.mvc.Http", false, this.getClass().getClassLoader());
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    protected NoticeReportResult submitError(Throwable error,
                               io.honeybadger.reporter.dto.Request request, Set<String> tags) {
        final String errorClassName = error.getClass().getName();
        if (errorClassName != null &&
                config.getExcludedClasses().contains(errorClassName)) {
            return null;
        }

        Notice notice = new Notice(config)
                .setError(new NoticeDetails(config, error, tags));

        if (request != null) {
            notice.setRequest(request);
        }

        for (int retries = 0; retries < 3; retries++) {
            try {
                String json = gson.toJson(notice);
                HttpResponse response = sendToHoneybadger(json)
                        .returnResponse();
                int responseCode = response.getStatusLine().getStatusCode();

                if (responseCode != 201)
                    logger.error("Honeybadger did not respond with the " +
                                 "correct code. Response was [{}]. Retries={}",
                                 responseCode, retries);
                else {
                    logger.debug("Honeybadger logged error correctly: [{}]",
                                 error.getMessage());
                    UUID id = parseErrorId(response, gson);

                    return new NoticeReportResult(id, notice, error);
                }
            } catch (IOException e) {
                String msg = String.format("There was an error when trying " +
                                           "to send the error to " +
                                           "Honeybadger. Retries=%d", retries);
                logger.error(msg, e);
                logger.error("Original Error", error);
                return null;
            }
        }

        return null;
    }

    private UUID parseErrorId(HttpResponse response, Gson gson)
            throws IOException {
        try (InputStream in = response.getEntity().getContent();
             Reader reader = new InputStreamReader(in)) {
            @SuppressWarnings("unchecked")
            HashMap<String, String> map =
                    (HashMap<String, String>)gson.fromJson(reader, HashMap.class);

            if (map.containsKey("id")) {
                return UUID.fromString(map.get("id"));
            } else {
                return null;
            }
        }
    }

    /**
     * Send an error encoded in JSON to the Honeybadger API.
     *
     * @param jsonError Error JSON payload
     * @return Status code from the Honeybadger API
     * @throws IOException thrown when a network was encountered
     */
    private Response sendToHoneybadger(String jsonError) throws IOException {
        URI honeybadgerUrl = URI.create(
                String.format("%s/%s", config.getHoneybadgerUrl(), "v1/notices"));
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
    private Request buildRequest(URI honeybadgerUrl, String jsonError) {
        Request request = Request
               .Post(honeybadgerUrl)
               .addHeader("X-API-Key", config.getApiKey())
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

        return request;
    }
}
