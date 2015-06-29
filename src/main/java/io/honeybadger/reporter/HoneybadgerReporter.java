package io.honeybadger.reporter;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.honeybadger.reporter.servlet.HttpServletRequestInfoGenerator;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.apache.http.entity.ContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.io.*;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.*;

/**
 * Reporter utility class that gives a simple interface for sending Java
 * {@link java.lang.Throwable} classes to the Honeybadger API.
 *
 * @author <a href="https://github.com/page1">page1</a>
 * @author <a href="https://github.com/dekobon">Elijah Zupancic</a>
 * @since 1.0.0
 */
public class HoneybadgerReporter implements ErrorReporter {
    /** System property key identifying the Honeybadger URL to use. */
    public static final String HONEYBADGER_URL_SYS_PROP_KEY =
            "honeybadger.url";
    /** System property key identifying the Honeybadger API key to use. */
    public static final String HONEYBADGER_API_KEY_SYS_PROP_KEY =
            "honeybadger.api_key";

    /** Comma delinated list of system properties to not include. */
    public static final String HONEYBADGER_EXCLUDED_PROPS_SYS_PROP_KEY =
            "honeybadger.excluded_sys_props";

    /** Comma delinated list of exception classes to ignore. */
    public static final String HONEYBADGER_EXCLUDED_CLASSES_SYS_PROP_KEY =
            "honeybadger.excluded_exception_classes";

    public static final String DEFAULT_API_URI =
            "https://api.honeybadger.io/v1/notices";

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final String hostname;
    private final String runtimeRoot;
    private final Set<String> excludedSysProps;
    private final Set<String> excludedExceptionClasses;

    public HoneybadgerReporter() {
        this.hostname = hostname();
        this.runtimeRoot = runtimeRoot();
        this.excludedSysProps = buildExcludedSysProps();
        this.excludedExceptionClasses = buildExcludedExceptionClasses();
    }

    /**
     * Send any Java {@link java.lang.Throwable} to the Honeybadger error
     * reporting interface.
     *
     * @param error error to report
     * @return UUID of error created, if there was a problem null
     */
    @Override
    public UUID reportError(Throwable error) {
        return submitError(error, new JsonObject());
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
    public UUID reportError(Throwable error, Object request) {
        if (error == null) { return null; }

        try {
            Class.forName("javax.servlet.http.HttpServletRequest");
            RequestInfoGenerator<?> generator =
                    new HttpServletRequestInfoGenerator();
            JsonObject jsonRequest = generator.routeRequest(request);
            return submitError(error, jsonRequest);
        } catch (ClassNotFoundException e) {
            return submitError(error, null);
        }
    }

    protected UUID submitError(Throwable error, JsonObject request) {
        JsonObject context = new JsonObject();

        context.addProperty("full_stacktrace", stacktraceAsString(error));
        request.add("context", context);

        final String errorClassName = error.getClass().getName();
        if (errorClassName != null &&
                excludedExceptionClasses.contains(errorClassName)) {
            return null;
        }

        Gson myGson = new Gson();
        JsonObject jsonError = new JsonObject();
        jsonError.add("notifier", makeNotifier());
        jsonError.add("error", makeError(error));

        if (request != null) {
            jsonError.add("request", request);
        }

        jsonError.add("server", makeServer());

        for (int retries = 0; retries < 3; retries++) {
            try {
                HttpResponse response = sendToHoneybadger(myGson.toJson(jsonError))
                        .returnResponse();
                int responseCode = response.getStatusLine().getStatusCode();

                if (responseCode != 201)
                    logger.error("Honeybadger did not respond with the " +
                                 "correct code. Response was [{}]. Retries={}",
                                 responseCode, retries);
                else {
                    logger.debug("Honeybadger logged error correctly: {}",
                                 error);
                    return parseErrorId(response, myGson);
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

    /*
      Identify the notifier
    */
    private JsonObject makeNotifier() {
        JsonObject notifier = new JsonObject();
        notifier.addProperty("name", "honeybadger-jvm-client-v2");
        notifier.addProperty("version", "1.3.0");
        return notifier;
    }

    /*
      Format the throwable into a json object
    */
    private JsonObject makeError(Throwable error) {
        JsonObject jsonError = new JsonObject();
        jsonError.addProperty("class", error.getClass().getName());
        jsonError.addProperty("message", error.getMessage());

        JsonArray backTrace = new JsonArray();
        for (StackTraceElement trace : error.getStackTrace()) {
            JsonObject jsonTraceElement = new JsonObject();
            jsonTraceElement.addProperty("number", trace.getLineNumber());
            jsonTraceElement.addProperty("file", trace.getFileName());
            jsonTraceElement.addProperty("method",
                    String.format("%s.%s",
                            trace.getClassName(), trace.getMethodName()));
            backTrace.add(jsonTraceElement);
        }
        jsonError.add("backtrace", backTrace);

        JsonObject sourceElement = new JsonObject();

        appendStacktraceToJsonElement(error, sourceElement);

        jsonError.add("source", sourceElement);

        return jsonError;
    }

    private String stacktraceAsString(Throwable error) {
        final StringWriter sw = new StringWriter();
        final PrintWriter pw = new PrintWriter(sw, true);
        error.printStackTrace(pw);
        return sw.getBuffer().toString();
    }

    /**
     * Created a Honeybadger source blob compatible full stacktrace.
     */
    private void appendStacktraceToJsonElement(Throwable error,
                                               JsonObject json) {
        String stack = stacktraceAsString(error);
        Scanner scanner = new Scanner(stack);

        int lineNo = 0;

        while (scanner.hasNext()) {
            json.addProperty(String.valueOf(++lineNo), scanner.nextLine());
        }
    }

    private JsonObject makeServer() {
        JsonObject jsonServer = new JsonObject();
        jsonServer.addProperty("environment_name", environment());
        jsonServer.addProperty("hostname", hostname);
        jsonServer.addProperty("runtime_root", runtimeRoot);
        jsonServer.add("mdc_properties", mdcProperties());
        jsonServer.add("system_properties", systemProperties());

        return jsonServer;
    }

    private JsonObject mdcProperties() {
        JsonObject jsonMdc = new JsonObject();

        @SuppressWarnings("unchecked")
        Map<String, String> mdc = MDC.getCopyOfContextMap();

        if (mdc != null) {
            for (Map.Entry<String, String> entry : mdc.entrySet()) {
                jsonMdc.addProperty(entry.getKey(), entry.getValue());
            }
        }

        return jsonMdc;
    }

    private JsonObject systemProperties() {
        JsonObject jsonSysProps = new JsonObject();

        for (Map.Entry<Object, Object> entry: System.getProperties().entrySet()) {
            // We skip all excluded properties
            if (excludedSysProps.contains(entry.getKey().toString())) {
                continue;
            }

            jsonSysProps.addProperty(entry.getKey().toString(),
                                     entry.getValue().toString());
        }

        return jsonSysProps;
    }

    private Set<String> buildExcludedSysProps() {
        String excluded = System.getProperty(HONEYBADGER_EXCLUDED_PROPS_SYS_PROP_KEY);
        HashSet<String> set = new HashSet<>();

        set.add(HONEYBADGER_API_KEY_SYS_PROP_KEY);
        set.add(HONEYBADGER_EXCLUDED_PROPS_SYS_PROP_KEY);
        set.add(HONEYBADGER_URL_SYS_PROP_KEY);

        if (excluded == null || excluded.isEmpty()) {
            return set;
        }

        for (String item : excluded.split(",")) {
            set.add(item);
        }

        return set;
    }

    private Set<String> buildExcludedExceptionClasses() {
        String excluded = System.getProperty(HONEYBADGER_EXCLUDED_CLASSES_SYS_PROP_KEY);
        HashSet<String> set = new HashSet<>();

        if (excluded == null || excluded.isEmpty()) {
            return set;
        }

        for (String item : excluded.split(",")) {
            set.add(item);
        }

        return set;
    }

    /**
     * Send an error encoded in JSON to the Honeybadger API.
     *
     * @param jsonError Error JSON payload
     * @return Status code from the Honeybadger API
     * @throws IOException thrown when a network was encountered
     */
    private Response sendToHoneybadger(String jsonError) throws IOException {
        URI honeybadgerUrl = honeybadgerUrl();
        Request request = buildRequest(honeybadgerUrl, jsonError);
        Response response = request.execute();

        return response;
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
                System.getProperty(HONEYBADGER_API_KEY_SYS_PROP_KEY);

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
                    System.getProperty(HONEYBADGER_URL_SYS_PROP_KEY);

            if (sysProp != null) {
                url = sysProp;
            } else {
                url = DEFAULT_API_URI;
            }

            return URI.create(url);
        } catch (IllegalArgumentException e) {
            String format = "Honeybadger URL was not correctly formed. " +
                            "Double check the [%s] system property and " +
                            "verify that it is a valid URL.";
            String msg = String.format(format, HONEYBADGER_URL_SYS_PROP_KEY);

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

    /**
     * Attempt to find the hostname of the system reporting the error to
     * Honeybadger.
     *
     * @return the hostname of the system reporting the error, "unknown" if not found
     */
    private String hostname() {
        String host;

        if (System.getenv("HOSTNAME") != null) {
            host = System.getenv("HOSTNAME");
        } else if (System.getenv("COMPUTERNAME") != null) {
            host = System.getenv("COMPUTERNAME");
        } else {
            try {
                host = InetAddress.getLocalHost().getHostName();
            } catch (UnknownHostException e) {
                logger.error("Unable to find hostname", e);
                host = "unknown";
            }

        }

        return host;
    }

    private String runtimeRoot() {
        try {
            return (new File(".")).getCanonicalPath();
        } catch (IOException e) {
            logger.error("Can't get runtime root path", e);
            return "unknown";
        }
    }
}
