package org.dekobon.honeybadger;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.apache.http.HttpHost;
import org.apache.http.HttpVersion;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.apache.http.entity.ContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;

/**
 * Reporter utility class that gives a simple interface for sending Java
 * {@link java.lang.Throwable} classes to the Honeybadger API.
 *
 * @author <a href="https://github.com/page1">page1</a>
 * @author <a href="https://github.com/dekobon">dekobon</a>
 * @since 1.0.0
 */
public class HoneybadgerReporter {
    /** System property key identifying the Honeybadger URL to use. */
    public static final String HONEY_BADGER_URL_SYS_PROP_KEY =
            "honeybadger.url";
    /** System property key identifying the Honeybadger API key to use. */
    public static final String HONEY_BADGER_API_KEY_SYS_PROP_KEY =
            "honeybadger.api_key";

    private Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * Send any Java {@link java.lang.Throwable} to the Honeybadger error
     * reporting interface.
     *
     * @param error error to report
     */
    public void reportError(Throwable error) {
        Gson myGson = new Gson();
        JsonObject jsonError = new JsonObject();
        jsonError.add("notifier", makeNotifier());
        jsonError.add("error", makeError(error));
        jsonError.add("server", makeServer());

        for (int retries = 0; retries < 3; retries++) {
            try {
                int responseCode = sendToHoneybadger(myGson.toJson(jsonError));
                if (responseCode != 201)
                    logger.error("Honeybadger did not respond with the " +
                                 "correct code. Response was [{}]. Retries={}",
                                 responseCode, retries);
                else {
                    logger.debug("Honeybadger logged error correctly: {}",
                                 error);
                    break;
                }
            } catch (IOException e) {
                String msg = String.format("There was an error when trying " +
                                           "to send the error to " +
                                           "Honeybadger. Retries=%d", retries);
                logger.error(msg, e);
            }
        }
    }

    /*
      Identify the notifier
    */
    private JsonObject makeNotifier() {
        JsonObject notifier = new JsonObject();
        notifier.addProperty("name", "honeybadger-jvm-client-v2");
        notifier.addProperty("version", "1.0.0");
        return notifier;
    }

    /*
      Format the throwable into a json object
    */
    private JsonObject makeError(Throwable error) {
        JsonObject jsonError = new JsonObject();
        jsonError.addProperty("class", error.toString());

        JsonArray backTrace = new JsonArray();
        for (StackTraceElement trace : error.getStackTrace()) {
            JsonObject jsonTraceElement = new JsonObject();
            jsonTraceElement.addProperty("number", trace.getLineNumber());
            jsonTraceElement.addProperty("file", trace.getFileName());
            jsonTraceElement.addProperty("method", trace.getMethodName());
            backTrace.add(jsonTraceElement);
        }
        jsonError.add("backtrace", backTrace);

        return jsonError;
    }

    /*
      Establish the environment
    */
    private JsonObject makeServer() {
        JsonObject jsonServer = new JsonObject();
        jsonServer.addProperty("environment_name", environment());
        return jsonServer;
    }

    /**
     * Send an error encoded in JSON to the Honeybadger API.
     *
     * @param jsonError Error JSON payload
     * @return Status code from the Honeybadger API
     * @throws IOException thrown when a network was encountered
     */
    private int sendToHoneybadger(String jsonError) throws IOException {
        URI honeybadgerUrl = honeybadgerUrl();
        Request request = buildRequest(honeybadgerUrl, jsonError);
        Response response = request.execute();

        return response.returnResponse().getStatusLine().getStatusCode();
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
        final String honeybadgerApiKey =
                System.getProperty(HONEY_BADGER_API_KEY_SYS_PROP_KEY);

        Request request = Request
               .Post(honeybadgerUrl)
               .addHeader("X-API-Key", honeybadgerApiKey)
               .addHeader("Accept", "application/json")
               .version(HttpVersion.HTTP_1_1)
               .bodyString(jsonError, ContentType.APPLICATION_JSON);

        if (System.getProperty("http.proxyHost") != null) {
            int port = Integer.parseInt(System.getProperty("http.proxyPort"));
            HttpHost proxy = new HttpHost(System.getProperty("http.proxyHost"),
                                          port);

            request.viaProxy(proxy);
        }

        return request;
    }

    /**
     * Finds the Honeybadger endpoint to send erros to.
     *
     * @return the default URL unless it is overriden by a system property
     */
    private URI honeybadgerUrl() {
        try {
            final String url;
            final String sysProp =
                    System.getProperty(HONEY_BADGER_URL_SYS_PROP_KEY);

            if (sysProp != null) {
                url = sysProp;
            } else {
                url = "https://api.honeybadger.io/v1/notices";
            }

            return URI.create(url);
        } catch (IllegalArgumentException e) {
            String format = "Honeybadger URL was not correctly formed. " +
                            "Double check the [%s] system property and " +
                            "verify that it is a valid URL.";
            String msg = String.format(format, HONEY_BADGER_URL_SYS_PROP_KEY);

            throw new HoneybadgerException(msg, e);
        }
    }

    /**
     * Finds the name of the environment by looking at a few common Java
     * system properties and/or environment variables.
     *
     * @return the name of the environment, otherwise "development"
     */
    private String environment() {
        String sysPropJavaEnv = System.getProperty("JAVA_ENV");
        if (sysPropJavaEnv != null) return sysPropJavaEnv;

        String javaEnv = System.getenv("JAVA_ENV");
        if (javaEnv != null) return javaEnv;

        String sysPropEnv = System.getProperty("ENV");
        if (sysPropEnv != null) return sysPropEnv;

        String env = System.getenv("ENV");
        if (sysPropEnv != null) return env;

        // If no system property defined, then return development
        return "development";
    }
}
