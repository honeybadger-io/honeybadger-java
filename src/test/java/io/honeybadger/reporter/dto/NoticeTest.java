package io.honeybadger.reporter.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.report.*;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import com.github.fge.jsonschema.main.JsonValidator;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.honeybadger.reporter.config.ConfigContext;
import io.honeybadger.reporter.config.SystemSettingsConfigContext;
import io.honeybadger.reporter.servlet.FakeHttpServletRequest;
import org.junit.Test;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class NoticeTest {
    private static final String JSON_SCHEMA_URL =
            "https://gist.githubusercontent.com/joshuap/94901ba378fd09a783be/raw/b632ff0a6b1ec82ced73735a321f1e44e94669d2/notices.json";

    private final ObjectMapper mapper = new ObjectMapper();
    private final JsonNode schema;

    {
        try {
            this.schema = mapper.readTree(new URL(JSON_SCHEMA_URL));
        } catch (IOException e) {
            throw new RuntimeException("Couldn't get JSON schema", e);
        }
    }

    private ConfigContext config = new SystemSettingsConfigContext();

    Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    @Test
    public void canSerializeReportedErrorWithoutRequest() throws Exception {
        Exception e = new RuntimeException("Test exception");
        Notice error = new Notice(config)
                .setError(new NoticeDetails(config, e));
        validateReportedErrorJson(error);
    }

    @Test
    public void canSerializeReportedErrorWithRequest() throws Exception {
        Exception e = new RuntimeException("Test exception");
        HttpServletRequest request = new FakeHttpServletRequest();

        Notice error = new Notice(config)
                .setError(new NoticeDetails(config, e))
                .setRequest(HttpServletRequestFactory.create(config, request));
        validateReportedErrorJson(error);
    }

    @Test
    public void canSerializeReportedErrorWithDetailedRequest() throws Exception {
        Exception e = new RuntimeException("Test exception");
        ArrayList<String> cookies = new ArrayList<>(ImmutableList.of(
                "theme=light",
                "sessionToken=abc123; Expires=Wed, 09 Jun 2021 10:18:14 GMT",
                "multi-value=true; lastItem=true"));
        Map<String, ArrayList<String>> headers = ImmutableMap.of(
                "set-cookie", cookies
        );

        HttpServletRequest request = new FakeHttpServletRequest(headers);

        Notice error = new Notice(config)
                .setError(new NoticeDetails(config, e))
                .setRequest(HttpServletRequestFactory.create(config, request));
        validateReportedErrorJson(error);
    }

    @Test
    public void canSerializeChainedReportedErrorWithoutRequest() throws Exception {
        Exception origin = new RuntimeException("This is the cause");
        Exception e = new RuntimeException("Test exception", origin);
        Notice error = new Notice(config)
                .setError(new NoticeDetails(config, e));
        validateReportedErrorJson(error);
    }

    @Test
    public void canSerializeChainedReportedErrorWithRequest() throws Exception {
        Exception origin = new RuntimeException("This is the cause");
        Exception e = new RuntimeException("Test exception", origin);
        HttpServletRequest request = new FakeHttpServletRequest();

        Notice error = new Notice(config)
                .setError(new NoticeDetails(config, e))
                .setRequest(HttpServletRequestFactory.create(config, request));
        validateReportedErrorJson(error);
    }

    private void validateReportedErrorJson(Notice error)
            throws ProcessingException, IOException {
        String jsonText = gson.toJson(error).toString();

        JsonSchemaFactory factory = JsonSchemaFactory.byDefault();
        JsonValidator validator = factory.byDefault().getValidator();

        JsonNode jsonNode = mapper.readTree(jsonText);

        ProcessingReport report = validator.validate(schema, jsonNode);

        if (!report.isSuccess()) {
            Iterator<ProcessingMessage> itr = report.iterator();

            StringBuilder builder = new StringBuilder()
                    .append(System.lineSeparator());

            while (itr.hasNext()) {
                ProcessingMessage msg = itr.next();
                builder.append(msg.toString()).append(System.lineSeparator());
            }

            builder.append(System.lineSeparator()).append(jsonText);

            fail(builder.toString());
        } else {
            assertTrue("Generated JSON validated correctly", true);
        }
    }
}
