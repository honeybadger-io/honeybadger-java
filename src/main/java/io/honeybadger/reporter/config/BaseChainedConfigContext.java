package io.honeybadger.reporter.config;

import java.net.URI;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static io.honeybadger.reporter.config.MapConfigContext.*;

/**
 * Abstract implementation of {@link ConfigContext} that allows for chaining
 * in default implementations of configuration that are delegate to when
 * we aren't passed a value.
 *
 * @author <a href="https://github.com/dekobon">Elijah Zupancic</a>
 * @since 1.0.10
 */
public abstract class BaseChainedConfigContext implements ConfigContext {
    protected String environment;
    protected URI honeybadgerUrl;
    protected String apiKey;
    protected Set<String> excludedSysProps;
    protected Set<String> excludedParams;
    protected Set<String> excludedClasses;
    protected String applicationPackage;
    protected String honeybadgerReadApiKey;
    protected Boolean feedbackFormDisplayed;
    protected String feedbackFormPath;
    protected String httpProxyHost;
    protected Integer httpProxyPort;

    /** Singleton instance of default configuration for easy reference. */
    public static final ConfigContext DEFAULT_CONFIG =
            new DefaultsConfigContext();

    /**
     * Constructor that prepopulates configuration context with the default
     * values.
     */
    public BaseChainedConfigContext() {
        this(DEFAULT_CONFIG);
    }

    /**
     * Constructor that takes a default value for each one of the configuration
     * values.
     *
     * @param defaultingContext context that provides default values
     */
    public BaseChainedConfigContext(ConfigContext defaultingContext) {
        overwriteWithContext(defaultingContext);
    }

    @Override
    public String getEnvironment() {
        return environment;
    }

    @Override
    public URI getHoneybadgerUrl() {
        return honeybadgerUrl;
    }

    @Override
    public String getApiKey() {
        return apiKey;
    }

    @Override
    public Set<String> getExcludedSysProps() {
        return excludedSysProps;
    }

    @Override
    public Set<String> getExcludedParams() {
        return excludedParams;
    }

    @Override
    public Set<String> getExcludedClasses() {
        return excludedClasses;
    }

    @Override
    public String getApplicationPackage() {
        return applicationPackage;
    }

    @Override
    public String getHoneybadgerReadApiKey() {
        return honeybadgerReadApiKey;
    }

    @Override
    public Boolean isFeedbackFormDisplayed() {
        return feedbackFormDisplayed;
    }

    @Override
    public String getFeedbackFormPath() {
        return feedbackFormPath;
    }

    @Override
    public String getHttpProxyHost() {
        return httpProxyHost;
    }

    @Override
    public Integer getHttpProxyPort() {
        return httpProxyPort;
    }

    /**
     * Overwrites the configuration values with the values of the passed context
     * if those values are not null and aren't empty.
     *
     * @param context context to overwrite configuration with
     */
    public void overwriteWithContext(ConfigContext context) {
        if (isPresent(context.getEnvironment())) {
            this.environment = context.getEnvironment();
        }

        if (context.getHoneybadgerUrl() != null) {
            this.honeybadgerUrl = context.getHoneybadgerUrl();
        }

        if (isPresent(context.getApiKey())) {
            this.apiKey = context.getApiKey();
        }

        if (isPresent(context.getExcludedSysProps())) {
            // We always create a new instance so we can be sure that these
            // default values are included
            Set<String> set = new HashSet<>(context.getExcludedSysProps());
            set.add(HONEYBADGER_API_KEY);
            set.add(HONEYBADGER_EXCLUDED_PROPS_KEY);
            set.add(HONEYBADGER_URL_KEY);

            this.excludedSysProps = set;
        }

        if (isPresent(context.getExcludedParams())) {
            this.excludedParams = context.getExcludedParams();
        }

        if (isPresent(context.getExcludedClasses())) {
            this.excludedClasses = context.getExcludedClasses();
        }

        if (isPresent(context.getApplicationPackage())) {
            this.applicationPackage = context.getApplicationPackage();
        }

        if (isPresent(context.getHoneybadgerReadApiKey())) {
            this.honeybadgerReadApiKey = context.getHoneybadgerReadApiKey();
        }

        if (context.isFeedbackFormDisplayed() != null) {
            this.feedbackFormDisplayed = context.isFeedbackFormDisplayed();
        }

        if (isPresent(context.getFeedbackFormPath())) {
            this.feedbackFormPath = context.getFeedbackFormPath();
        }
    }

    protected boolean isPresent(Collection<?> collection) {
        if (collection == null) return false;
        if (collection.isEmpty()) return false;

        return true;
    }

    protected boolean isPresent(CharSequence charSequence) {
        if (charSequence != null) return true;
        if (charSequence.length() > 0) return true;

        return false;
    }

    public BaseChainedConfigContext setEnvironment(String environment) {
        this.environment = environment;
        return this;
    }

    public BaseChainedConfigContext setHoneybadgerUrl(URI honeybadgerUrl) {
        this.honeybadgerUrl = honeybadgerUrl;
        return this;
    }

    public BaseChainedConfigContext setApiKey(String apiKey) {
        this.apiKey = apiKey;
        return this;
    }

    public BaseChainedConfigContext setExcludedSysProps(Set<String> excludedSysProps) {
        this.excludedSysProps = excludedSysProps;
        return this;
    }

    public BaseChainedConfigContext setExcludedParams(Set<String> excludedParams) {
        this.excludedParams = excludedParams;
        return this;
    }

    public BaseChainedConfigContext setExcludedClasses(Set<String> excludedClasses) {
        this.excludedClasses = excludedClasses;
        return this;
    }

    public BaseChainedConfigContext setApplicationPackage(String applicationPackage) {
        this.applicationPackage = applicationPackage;
        return this;
    }

    public BaseChainedConfigContext setHoneybadgerReadApiKey(String honeybadgerReadApiKey) {
        this.honeybadgerReadApiKey = honeybadgerReadApiKey;
        return this;
    }

    public BaseChainedConfigContext setHttpProxyHost(String httpProxyHost) {
        this.httpProxyHost = httpProxyHost;
        return this;
    }

    public BaseChainedConfigContext setHttpProxyPort(Integer httpProxyPort) {
        this.httpProxyPort = httpProxyPort;
        return this;
    }

    public Boolean setFeedbackFormDisplayed(Boolean feedbackFormDisplayed) {
        if (feedbackFormDisplayed == null) {
            throw new IllegalArgumentException(
                    "This value should be only null during initialization");
        }

        this.feedbackFormDisplayed = feedbackFormDisplayed;
        return feedbackFormDisplayed;
    }

    public BaseChainedConfigContext setFeedbackFormPath(String feedbackFormPath) {
        this.feedbackFormPath = feedbackFormPath;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BaseChainedConfigContext that = (BaseChainedConfigContext) o;

        if (environment != null ? !environment.equals(that.environment) : that.environment != null)
            return false;
        if (honeybadgerUrl != null ? !honeybadgerUrl.equals(that.honeybadgerUrl) : that.honeybadgerUrl != null)
            return false;
        if (apiKey != null ? !apiKey.equals(that.apiKey) : that.apiKey != null)
            return false;
        if (excludedSysProps != null ? !excludedSysProps.equals(that.excludedSysProps) : that.excludedSysProps != null)
            return false;
        if (excludedParams != null ? !excludedParams.equals(that.excludedParams) : that.excludedParams != null)
            return false;
        if (excludedClasses != null ? !excludedClasses.equals(that.excludedClasses) : that.excludedClasses != null)
            return false;
        if (applicationPackage != null ? !applicationPackage.equals(that.applicationPackage) : that.applicationPackage != null)
            return false;
        if (honeybadgerReadApiKey != null ? !honeybadgerReadApiKey.equals(that.honeybadgerReadApiKey) : that.honeybadgerReadApiKey != null)
            return false;
        if (feedbackFormDisplayed != null ? !feedbackFormDisplayed.equals(that.feedbackFormDisplayed) : that.feedbackFormDisplayed != null)
            return false;
        if (feedbackFormPath != null ? !feedbackFormPath.equals(that.feedbackFormPath) : that.feedbackFormPath != null)
            return false;
        if (httpProxyHost != null ? !httpProxyHost.equals(that.httpProxyHost) : that.httpProxyHost != null)
            return false;
        return !(httpProxyPort != null ? !httpProxyPort.equals(that.httpProxyPort) : that.httpProxyPort != null);

    }

    @Override
    public int hashCode() {
        int result = environment != null ? environment.hashCode() : 0;
        result = 31 * result + (honeybadgerUrl != null ? honeybadgerUrl.hashCode() : 0);
        result = 31 * result + (apiKey != null ? apiKey.hashCode() : 0);
        result = 31 * result + (excludedSysProps != null ? excludedSysProps.hashCode() : 0);
        result = 31 * result + (excludedParams != null ? excludedParams.hashCode() : 0);
        result = 31 * result + (excludedClasses != null ? excludedClasses.hashCode() : 0);
        result = 31 * result + (applicationPackage != null ? applicationPackage.hashCode() : 0);
        result = 31 * result + (honeybadgerReadApiKey != null ? honeybadgerReadApiKey.hashCode() : 0);
        result = 31 * result + (feedbackFormDisplayed != null ? feedbackFormDisplayed.hashCode() : 0);
        result = 31 * result + (feedbackFormPath != null ? feedbackFormPath.hashCode() : 0);
        result = 31 * result + (httpProxyHost != null ? httpProxyHost.hashCode() : 0);
        result = 31 * result + (httpProxyPort != null ? httpProxyPort.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("BaseChainedConfigContext{");
        sb.append("environment='").append(environment).append('\'');
        sb.append(", honeybadgerUrl='").append(honeybadgerUrl).append('\'');
        sb.append(", apiKey='").append(apiKey).append('\'');
        sb.append(", excludedSysProps=").append(excludedSysProps);
        sb.append(", excludedParams=").append(excludedParams);
        sb.append(", excludedClasses=").append(excludedClasses);
        sb.append(", applicationPackage='").append(applicationPackage).append('\'');
        sb.append(", honeybadgerReadApiKey='").append(honeybadgerReadApiKey).append('\'');
        sb.append(", feedbackFormDisplayed=").append(feedbackFormDisplayed);
        sb.append(", feedbackFormPath='").append(feedbackFormPath).append('\'');
        sb.append(", httpProxyHost='").append(httpProxyHost).append('\'');
        sb.append(", httpProxyPort=").append(httpProxyPort);
        sb.append('}');
        return sb.toString();
    }
}
