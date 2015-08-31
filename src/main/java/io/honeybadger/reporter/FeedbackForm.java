package io.honeybadger.reporter;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;

import java.io.IOException;
import java.io.Writer;
import java.util.*;

/**
 * Utility class responsible for rendering the Honeybadger feedback form.
 *
 * @author <a href="https://github.com/dekobon">Elijah Zupancic</a>
 * @since 1.0.9
 */
public class FeedbackForm {
    protected final MustacheFactory mf = new DefaultMustacheFactory();
    protected final Locale defaultLocale = new Locale("en", "US");
    protected final Mustache mustache;
    protected final String actionURI;

    public FeedbackForm(String templatePath) {
        if (templatePath == null)
            throw new IllegalArgumentException("template path must not be null");

        this.mustache = mf.compile(templatePath);
        this.actionURI = actionURI();
    }

    protected String actionURI() {
        return String.format("%s/%s",
                HoneybadgerReporter.honeybadgerUrl(), "v1/feedback/");
    }

    public void renderHtml(Object errorId, Writer writer) throws IOException {
        renderHtml(errorId, writer, defaultLocale);
    }

    public void renderHtml(Object errorId, Writer writer, Locale locale) throws IOException {
        Locale selectedLocale = locale == null ? defaultLocale : locale;
        ResourceBundle messages = ResourceBundle.getBundle("i8n/feedback-form", selectedLocale);
        Map<String, String> scopes = new HashMap<>(30);

        // This could happen if the Honeybadger API is down
        if (errorId == null) {
            String msg = "<!DOCTYPE HTML>\n" +
                    "<html>\n" +
                    "<head><title>Error</title></head>" +
                    "<body><h1>An unknown error occurred</h1><body></html>";
            writer.append(msg);
            return;
        }

        scopes.put("error_id", errorId.toString());
        scopes.put("action", actionURI);

        Enumeration<String> enumeration = messages.getKeys();

        while (enumeration.hasMoreElements()) {
            String key = enumeration.nextElement();
            scopes.put(key, messages.getString(key));
        }

        mustache.execute(writer, scopes);
    }
}
