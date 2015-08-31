package io.honeybadger.reporter.reporter;

import io.honeybadger.reporter.FeedbackForm;
import org.junit.Test;

import java.io.StringWriter;
import java.util.UUID;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

public class FeedbackFormTest {
    @Test
    public void feedbackFormRendersTemplate() throws Exception {
        StringWriter writer = new StringWriter();
        String id = (new UUID(12L, 36L)).toString();
        FeedbackForm instance = new FeedbackForm("templates/feedback-form.mustache");

        instance.renderHtml(id, writer);

        assertNotNull("Template renderHtml should return *something*", writer.toString());
        assertFalse("Template renderHtml shouldn't be empty string", writer.toString().trim().isEmpty());
    }
}
