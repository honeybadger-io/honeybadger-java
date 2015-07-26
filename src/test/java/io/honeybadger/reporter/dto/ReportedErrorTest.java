package io.honeybadger.reporter.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsonschema.JsonSchema;
import com.fasterxml.jackson.databind.node.JsonNodeCreator;
import com.github.fge.jsonschema.cfg.ValidationConfiguration;
import com.github.fge.jsonschema.cfg.ValidationConfigurationBuilder;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.load.RefResolver;
import com.github.fge.jsonschema.core.load.SchemaLoader;
import com.github.fge.jsonschema.core.processing.CachingProcessor;
import com.github.fge.jsonschema.core.report.*;
import com.github.fge.jsonschema.library.DraftV3Library;
import com.github.fge.jsonschema.library.Library;
import com.github.fge.jsonschema.library.LibraryBuilder;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import com.github.fge.jsonschema.main.JsonValidator;
import com.github.fge.jsonschema.processors.data.SchemaContext;
import com.github.fge.jsonschema.processors.data.ValidatorList;
import com.github.fge.jsonschema.processors.validation.ValidationChain;
import com.github.fge.jsonschema.processors.validation.ValidationProcessor;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.honeybadger.reporter.servlet.FakeHttpServletRequest;
import org.junit.Test;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.IOException;
import java.net.URL;
import java.util.Iterator;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ReportedErrorTest {
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

    Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    @Test
    public void canSerializeReportedErrorWithoutRequest() throws Exception {
        Exception e = new RuntimeException("Test exception");
        ReportedError error = new ReportedError()
                .setError(new ErrorDetails(e));
        validateReportedErrorJson(error);
    }

    @Test
    public void canSerializeReportedErrorWithRequest() throws Exception {
        Exception e = new RuntimeException("Test exception");
        HttpServletRequest request = new FakeHttpServletRequest();

        ReportedError error = new ReportedError()
                .setError(new ErrorDetails(e))
                .setRequest(new Request(request));
        validateReportedErrorJson(error);
    }

    @Test
    public void canSerializeChainedReportedErrorWithoutRequest() throws Exception {
        Exception origin = new RuntimeException("This is the cause");
        Exception e = new RuntimeException("Test exception", origin);
        ReportedError error = new ReportedError()
                .setError(new ErrorDetails(e));
        validateReportedErrorJson(error);
    }

    @Test
    public void canSerializeChainedReportedErrorWithRequest() throws Exception {
        Exception origin = new RuntimeException("This is the cause");
        Exception e = new RuntimeException("Test exception", origin);
        HttpServletRequest request = new FakeHttpServletRequest();

        ReportedError error = new ReportedError()
                .setError(new ErrorDetails(e))
                .setRequest(new Request(request));
        validateReportedErrorJson(error);
    }

    private void validateReportedErrorJson(ReportedError error)
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
