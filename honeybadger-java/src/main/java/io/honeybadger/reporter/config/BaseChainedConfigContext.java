package io.honeybadger.reporter.config;

import java.net.URI;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import static io.honeybadger.reporter.config.MapConfigContext.*;
import static io.honeybadger.util.HBCollectionUtils.isPresent;
import static io.honeybadger.util.HBStringUtils.isPresent;

/**
 * Abstract implementation of {@link ConfigContext} that allows for chaining
 * in default implementations of configuration that are delegate to when
 * we aren't passed a value.
 *
 * @author <a href="https://github.com/dekobon">Elijah Zupancic</a>
 * @since 1.0.10
 */
public abstract class BaseChainedConfigContext implements ConfigContext {
    /**
     * Singleton instance of default configuration for easy reference.
     */
    public static final ConfigContext DEFAULT_CONFIG =
            new DefaultsConfigContext();
    private String environment;
    private URI honeybadgerUrl;
    private String apiKey;
    private Set<String> excludedSysProps = new HashSet<>();
    private Set<String> excludedParams = new HashSet<>();
    private Set<String> excludedClasses = new HashSet<>();
    private String applicationPackage;
    private String honeybadgerReadApiKey;
    private Boolean feedbackFormDisplayed;
    private String feedbackFormPath;
    private String httpProxyHost;
    private Integer httpProxyPort;
    private Integer maximumErrorReportingRetries;

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
    public BaseChainedConfigContext(final ConfigContext defaultingContext) {
        overwriteWithContext(defaultingContext);
    }

    @Override
    public String getEnvironment() {
        return environment;
    }

    public BaseChainedConfigContext setEnvironment(final String environment) {
        this.environment = environment;
        return this;
    }

    @Override
    public URI getHoneybadgerUrl() {
        return honeybadgerUrl;
    }

    public BaseChainedConfigContext setHoneybadgerUrl(final URI honeybadgerUrl) {
        this.honeybadgerUrl = honeybadgerUrl;
        return this;
    }

    @Override
    public String getApiKey() {
        return apiKey;
    }

    public BaseChainedConfigContext setApiKey(final String apiKey) {
        this.apiKey = apiKey;
        return this;
    }

    @Override
    public Set<String> getExcludedSysProps() {
        return excludedSysProps;
    }

    public BaseChainedConfigContext setExcludedSysProps(final Set<String> excludedSysProps) {
        this.excludedSysProps = excludedSysProps;
        return this;
    }

    @Override
    public Set<String> getExcludedParams() {
        return excludedParams;
    }

    public BaseChainedConfigContext setExcludedParams(final Set<String> excludedParams) {
        this.excludedParams = excludedParams;
        return this;
    }

    @Override
    public Set<String> getExcludedClasses() {
        return excludedClasses;
    }

    public BaseChainedConfigContext setExcludedClasses(final Set<String> excludedClasses) {
        this.excludedClasses = excludedClasses;
        return this;
    }

    @Override
    public String getApplicationPackage() {
        return applicationPackage;
    }

    public BaseChainedConfigContext setApplicationPackage(final String applicationPackage) {
        this.applicationPackage = applicationPackage;
        return this;
    }

    @Override
    public String getHoneybadgerReadApiKey() {
        return honeybadgerReadApiKey;
    }

    public BaseChainedConfigContext setHoneybadgerReadApiKey(final String honeybadgerReadApiKey) {
        this.honeybadgerReadApiKey = honeybadgerReadApiKey;
        return this;
    }

    @Override
    public Boolean isFeedbackFormDisplayed() {
        return getFeedbackFormDisplayed();
    }

    @Override
    public String getFeedbackFormPath() {
        return feedbackFormPath;
    }

    public BaseChainedConfigContext setFeedbackFormPath(final String feedbackFormPath) {
        this.feedbackFormPath = feedbackFormPath;
        return this;
    }

    @Override
    public String getHttpProxyHost() {
        return httpProxyHost;
    }

    public BaseChainedConfigContext setHttpProxyHost(final String httpProxyHost) {
        this.httpProxyHost = httpProxyHost;
        return this;
    }

    @Override
    public Integer getHttpProxyPort() {
        return httpProxyPort;
    }

    public BaseChainedConfigContext setHttpProxyPort(final Integer httpProxyPort) {
        this.httpProxyPort = httpProxyPort;
        return this;
    }

    @Override
    public Integer getMaximumErrorReportingRetries() {
        return maximumErrorReportingRetries;
    }

    public BaseChainedConfigContext getMaximumErrorReportingRetries(final Integer maximumRetries) {
        this.maximumErrorReportingRetries = maximumRetries;
        return this;
    }


    /**
     * Overwrites the configuration values with the values of the passed context
     * if those values are not null and aren't empty.
     *
     * @param context context to overwrite configuration with
     */
    public void overwriteWithContext(final ConfigContext context) {
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

        if (context.getMaximumErrorReportingRetries() != null) {
            this.maximumErrorReportingRetries = context.getMaximumErrorReportingRetries();
        }
    }

    @SuppressWarnings("HiddenField")
    public Boolean setFeedbackFormDisplayed(final Boolean feedbackFormDisplayed) {
        if (feedbackFormDisplayed == null) {
            throw new IllegalArgumentException(
                    "This value should be only null during initialization");
        }

        this.feedbackFormDisplayed = feedbackFormDisplayed;
        return feedbackFormDisplayed;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BaseChainedConfigContext that = (BaseChainedConfigContext) o;
        return Objects.equals(environment, that.environment) &&
                Objects.equals(honeybadgerUrl, that.honeybadgerUrl) &&
                Objects.equals(apiKey, that.apiKey) &&
                Objects.equals(excludedSysProps, that.excludedSysProps) &&
                Objects.equals(excludedParams, that.excludedParams) &&
                Objects.equals(excludedClasses, that.excludedClasses) &&
                Objects.equals(applicationPackage, that.applicationPackage) &&
                Objects.equals(honeybadgerReadApiKey, that.honeybadgerReadApiKey) &&
                Objects.equals(feedbackFormDisplayed, that.feedbackFormDisplayed) &&
                Objects.equals(feedbackFormPath, that.feedbackFormPath) &&
                Objects.equals(httpProxyHost, that.httpProxyHost) &&
                Objects.equals(httpProxyPort, that.httpProxyPort) &&
                Objects.equals(maximumErrorReportingRetries, that.maximumErrorReportingRetries);
    }

    @Override
    public String toString() {
        return "BaseChainedConfigContext{" +
                "environment='" + environment + '\'' +
                ", honeybadgerUrl=" + honeybadgerUrl +
                ", apiKey='" + apiKey + '\'' +
                ", excludedSysProps=" + excludedSysProps +
                ", excludedParams=" + excludedParams +
                ", excludedClasses=" + excludedClasses +
                ", applicationPackage='" + applicationPackage + '\'' +
                ", honeybadgerReadApiKey='" + honeybadgerReadApiKey + '\'' +
                ", feedbackFormDisplayed=" + feedbackFormDisplayed +
                ", feedbackFormPath='" + feedbackFormPath + '\'' +
                ", httpProxyHost='" + httpProxyHost + '\'' +
                ", httpProxyPort=" + httpProxyPort +
                ", maximumErrorReportingRetries=" + maximumErrorReportingRetries +
                '}';
    }

    @Override
    public int hashCode() {
        return Objects.hash(environment, honeybadgerUrl, apiKey, excludedSysProps, excludedParams,
                excludedClasses, applicationPackage, honeybadgerReadApiKey, feedbackFormDisplayed,
                feedbackFormPath, httpProxyHost, httpProxyPort, maximumErrorReportingRetries);
    }

    protected Boolean getFeedbackFormDisplayed() {
        return feedbackFormDisplayed;
    }
}
