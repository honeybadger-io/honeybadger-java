package io.honeybadger.reporter;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import io.honeybadger.reporter.config.ConfigContext;

import java.io.IOException;
import java.io.Writer;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Utility class responsible for rendering the Honeybadger feedback form.
 *
 * @author <a href="https://github.com/dekobon">Elijah Zupancic</a>
 * @since 1.0.9
 */
public class FeedbackForm {
    public static final int INITIAL_SCOPE_HASHMAP_CAPACITY = 30;
    private final ConfigContext config;

    private final MustacheFactory mf = new DefaultMustacheFactory();
    private final Locale defaultLocale = new Locale("en", "US");
    private final Mustache mustache;
    private final String actionURI;

    public FeedbackForm(final ConfigContext config) {
        String templatePath = config.getFeedbackFormPath();
        if (templatePath == null) {
            throw new IllegalArgumentException("template path must not be null");
        }
        this.config = config;
        this.mustache = getMf().compile(templatePath);
        this.actionURI = actionURI();
    }

    protected String actionURI() {
        return String.format("%s/%s", config.getHoneybadgerUrl(), "v1/feedback/");
    }

    public void renderHtml(final Object errorId, final String message, final Writer writer) throws IOException {
        renderHtml(errorId, message, writer, getDefaultLocale());
    }

    public void renderHtml(final Object errorId, final String message, final Writer writer, final Locale locale) throws IOException {
        Locale selectedLocale = locale == null ? getDefaultLocale() : locale;
        ResourceBundle messages = ResourceBundle.getBundle("i8n/feedback-form", selectedLocale);
        Map<String, String> scopes = new HashMap<>(INITIAL_SCOPE_HASHMAP_CAPACITY);

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

        if (message != null && !message.isEmpty()) {
            scopes.put("error_msg", message);
        }

        scopes.put("action", getActionURI());

        Enumeration<String> enumeration = messages.getKeys();

        while (enumeration.hasMoreElements()) {
            String key = enumeration.nextElement();
            scopes.put(key, messages.getString(key));
        }

        getMustache().execute(writer, scopes);
    }

    public MustacheFactory getMf() {
        return mf;
    }

    public Locale getDefaultLocale() {
        return defaultLocale;
    }

    public Mustache getMustache() {
        return mustache;
    }

    public String getActionURI() {
        return actionURI;
    }
}
