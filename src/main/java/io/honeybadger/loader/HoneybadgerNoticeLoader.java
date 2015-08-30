package io.honeybadger.loader;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import io.honeybadger.reporter.HoneybadgerReporter;
import io.honeybadger.reporter.NoticeReporter;
import io.honeybadger.reporter.dto.Notice;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.util.UUID;

/**
 * Utility class used to load a fault's details into a readable object
 * structure.
 */
public class HoneybadgerNoticeLoader {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final Gson gson = new GsonBuilder()
            .create();

    String pullFaultJson(UUID faultId) throws IOException {
        String readApiKey = readApiKey();

        if (readApiKey == null || readApiKey.isEmpty()) {
            String msg = String.format("Property %s must be set if you are " +
                    "going to be accessing the Read API", NoticeReporter.READ_API_KEY_PROP_KEY);
            throw new IllegalArgumentException(msg);
        }

        final URI baseURI = URI.create(String.format("%s/%s/%s",
                HoneybadgerReporter.honeybadgerUrl(), "v1/notices", faultId));

        String withAuth = String.format("%s/?auth_token=%s",
                baseURI, readApiKey);

        logger.debug("Querying for error details: {}", baseURI);

        Response response = Request
                .Get(withAuth)
                .addHeader("Accept", "application/json")
                .execute();

        return response.returnContent().asString();
    }

    /**
     * Finds the Read API key, preferring ENV to system properties.
     *
     * @return the API key if found, otherwise null
     */
    private static String readApiKey() {
        String envKey = System.getenv("HONEYBADGER_READ_API_KEY");
        if (envKey != null && !envKey.isEmpty()) return envKey;

        return System.getProperty(NoticeReporter.READ_API_KEY_PROP_KEY);
    }

    public Notice findErrorDetails(UUID faultId) throws IOException {
        String json = pullFaultJson(faultId);

        // HACK: Since our API is not symmetric, we do this in order to rename fields
        // and get *some* of the data that we sent.
        JsonObject originalJson = gson.fromJson(json, JsonObject.class).getAsJsonObject();
        JsonObject cgiData = originalJson.get("web_environment").getAsJsonObject();
        originalJson.get("request").getAsJsonObject().add("cgi_data", cgiData);

        Notice error = gson.fromJson(originalJson, Notice.class);
        return error;
    }
}
