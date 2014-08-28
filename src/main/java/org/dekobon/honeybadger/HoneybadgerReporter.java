package org.dekobon.honeybadger;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * @author Elijah Zupancic
 * @since 1.0.0
 */
public class HoneybadgerReporter {
    /** System property key identifying the honeybadger URL to use. */
    public static final String HONEY_BADGER_URL_SYS_PROP_KEY =
            "honeybadger.url";
    public static final String HONEY_BADGER_API_KEY_SYS_PROP_KEY =
            "honeybadger.api_key";

    private Logger logger = LoggerFactory.getLogger(getClass());

    public void reportErrorToHoneyBadger(Throwable error) {
        Gson myGson = new Gson();
        JsonObject jsonError = new JsonObject();
        jsonError.add("notifier", makeNotifier());
        jsonError.add("error", makeError(error));
        jsonError.add("server", makeServer());

        for (int retries = 0; retries < 3; retries++) {
            try {
                int responseCode = sendToHoneyBadger(myGson.toJson(jsonError));
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
        notifier.addProperty("name", "Honeybadger-java Notifier");
        notifier.addProperty("version", "1.3.0-1");
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

    /*
      Send the json string error to honeybadger
    */
    private int sendToHoneyBadger(String jsonError) throws IOException {
        URL honeybadgerUrl = honeybadgerUrl();
        HttpURLConnection conn = openConnection(honeybadgerUrl);
        int responseCode = -1;

        try (DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
             InputStreamReader reader = new InputStreamReader(conn.getInputStream());
             BufferedReader in = new BufferedReader(reader) ) {
            wr.writeBytes(jsonError);
            wr.flush();

            responseCode = conn.getResponseCode();
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
        }

        return responseCode;
    }

    private HttpURLConnection openConnection(URL honeybadgerUrl)
            throws IOException {
        final String honeybadgerApiKey =
                System.getProperty(HONEY_BADGER_API_KEY_SYS_PROP_KEY);

        final URLConnection rawConn = honeybadgerUrl.openConnection();
        @SuppressWarnings("unchecked")
        final HttpURLConnection conn = (HttpURLConnection)rawConn;

        //add request header
        conn.setRequestMethod("POST");
        conn.setRequestProperty("X-API-Key", honeybadgerApiKey);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Accept", "application/json");

        // Send post request
        conn.setDoOutput(true);

        return conn;
    }

    private URL honeybadgerUrl() {
        try {
            final String url;
            final String sysProp =
                    System.getProperty(HONEY_BADGER_URL_SYS_PROP_KEY);

            if (sysProp != null) {
                url = sysProp;
            } else {
                url = "https://api.honeybadger.io/v1/notices";
            }

            return new URL(url);
        } catch (MalformedURLException e) {
            String format = "Honeybadger URL was not correctly formed. " +
                            "Double check the [%s] system property and " +
                            "verify that it is a valid URL.";
            String msg = String.format(format, HONEY_BADGER_URL_SYS_PROP_KEY);

            throw new HoneybadgerException(msg, e);
        }
    }

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
