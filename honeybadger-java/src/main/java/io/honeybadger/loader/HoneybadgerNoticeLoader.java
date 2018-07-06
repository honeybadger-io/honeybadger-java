package io.honeybadger.loader;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.InjectableValues;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.honeybadger.reporter.config.ConfigContext;
import io.honeybadger.reporter.dto.Notice;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.UUID;

/**
 * Utility class used to load a fault's details into a readable object
 * structure.
 *
 * This class is currently only suitable for consumption by integration tests,
 * as much of the data in the reporter dto is not returned by the Notice API.
 * If you have an use case for Notice deserialization, please file an issue
 * at https://github.com/honeybadger-io/honeybadger-java
 *
 * @author <a href="https://github.com/dekobon">Elijah Zupancic</a>
 * @since 1.0.9
 */
public class HoneybadgerNoticeLoader {
    private static final int RETRIES = 3;
    public static final int RETRY_DELAY_MILLIS = 5000;
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
                    .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                    .configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false);

    private ConfigContext config;

    public HoneybadgerNoticeLoader(final ConfigContext config) {
        this.config = config;
    }

    String pullFaultJson(final UUID faultId) throws IOException {
        String readApiKey = config.getHoneybadgerReadApiKey();

        if (readApiKey == null) {
            String msg = "Read API key must be set";
            throw new IllegalArgumentException(msg);
        }

        final URI baseURI = URI.create(String.format("%s/%s/%s",
                config.getHoneybadgerUrl(), "v1/notices", faultId));

        String withAuth = String.format("%s/?auth_token=%s",
                baseURI, readApiKey);

        logger.debug("Querying for error details: {}", baseURI);

        Response response;
        HttpResponse httpResponse = null;

        // We loop here because the API returns 404 when the notice still
        // hasn't finished processing
        for (int i = 0; i < RETRIES; i++) {
            response = Request.Get(withAuth)
                              .addHeader("Accept", "application/json")
                              .execute();

            httpResponse = response.returnResponse();

            if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                break;
            }
            try {
                Thread.sleep(RETRY_DELAY_MILLIS);
            } catch (InterruptedException e) {
                break;
            }

            if  (i == RETRIES - 1) {
                String msg = String.format("Unable to get notice from API.\n" +
                                "[Response Status Code=%d]\n" +
                                "[Response Reason=%s]",
                        httpResponse.getStatusLine().getStatusCode(),
                        httpResponse.getStatusLine().getReasonPhrase());
                throw new IllegalArgumentException(msg);
            }
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        httpResponse.getEntity().writeTo(out);

        return out.toString();
    }

    public Notice findErrorDetails(final UUID faultId) throws IOException {
        String jsonText = pullFaultJson(faultId);

        // HACK: Since our API is not symmetric, we do this in order to rename fields
        // and get *some* of the data that we sent.
        JsonNode originalJson = OBJECT_MAPPER.readTree(jsonText);
        JsonNode cgiData = originalJson.get("web_environment");
        ((ObjectNode)originalJson.get("request"))
                .replace("cgi_data", cgiData);
        Notice error;

        InjectableValues.Std injectableValues = new InjectableValues.Std();
        OBJECT_MAPPER.setInjectableValues(injectableValues);
        injectableValues.addValue("config", config);

        error = OBJECT_MAPPER.readValue(originalJson.toString(), Notice.class);
        return error;
    }
}
