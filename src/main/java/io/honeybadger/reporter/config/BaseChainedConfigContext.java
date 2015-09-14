package io.honeybadger.reporter.config;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static io.honeybadger.reporter.config.MapConfigContext.HONEYBADGER_API_KEY;
import static io.honeybadger.reporter.config.MapConfigContext.HONEYBADGER_EXCLUDED_PROPS_KEY;
import static io.honeybadger.reporter.config.MapConfigContext.HONEYBADGER_URL_KEY;

/**
 * Abstract implementation of {@link ConfigContext} that allows for chaining
 * in default implementations of configuration that are delegate to when
 * we aren't passed a value.
 *
 * @author <a href="https://github.com/dekobon">Elijah Zupancic</a>
 * @since 1.0.10
 */
public abstract class BaseChainedConfigContext implements ConfigContext {
    protected String honeybadgerUrl;
    protected String apiKey;
    protected Set<String> excludedSysProps;
    protected Set<String> excludedParams;
    protected Set<String> excludedClasses;
    protected String applicationPackage;
    protected String honeybadgerReadApiKey;
    protected Boolean feedbackFormDisplayed;
    protected String feedbackFormPath;

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
    public String getHoneybadgerUrl() {
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

    /**
     * Overwrites the configuration values with the values of the passed context
     * if those values are not null and aren't empty.
     *
     * @param context context to overwrite configuration with
     */
    public void overwriteWithContext(ConfigContext context) {
        if (isPresent(context.getHoneybadgerUrl())) {
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

    public String setHoneybadgerUrl(String honeybadgerUrl) {
        this.honeybadgerUrl = honeybadgerUrl;
        return honeybadgerUrl;
    }

    public String setApiKey(String apiKey) {
        this.apiKey = apiKey;
        return apiKey;
    }

    public Set<String> setExcludedSysProps(Set<String> excludedSysProps) {
        this.excludedSysProps = excludedSysProps;
        return excludedSysProps;
    }

    public Set<String> setExcludedParams(Set<String> excludedParams) {
        this.excludedParams = excludedParams;
        return excludedParams;
    }

    public Set<String> setExcludedClasses(Set<String> excludedClasses) {
        this.excludedClasses = excludedClasses;
        return excludedClasses;
    }

    public String setApplicationPackage(String applicationPackage) {
        this.applicationPackage = applicationPackage;
        return applicationPackage;
    }

    public String setHoneybadgerReadApiKey(String honeybadgerReadApiKey) {
        this.honeybadgerReadApiKey = honeybadgerReadApiKey;
        return honeybadgerReadApiKey;
    }

    public Boolean setFeedbackFormDisplayed(Boolean feedbackFormDisplayed) {
        if (feedbackFormDisplayed == null) {
            throw new IllegalArgumentException(
                    "This value should be only null during initialization");
        }

        this.feedbackFormDisplayed = feedbackFormDisplayed;
        return feedbackFormDisplayed;
    }

    public String setFeedbackFormPath(String feedbackFormPath) {
        this.feedbackFormPath = feedbackFormPath;
        return feedbackFormPath;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BaseChainedConfigContext that = (BaseChainedConfigContext) o;

        if (!honeybadgerUrl.equals(that.honeybadgerUrl)) return false;
        if (!apiKey.equals(that.apiKey)) return false;
        if (!excludedSysProps.equals(that.excludedSysProps)) return false;
        if (!excludedParams.equals(that.excludedParams)) return false;
        if (!excludedClasses.equals(that.excludedClasses)) return false;
        if (!applicationPackage.equals(that.applicationPackage)) return false;
        if (!honeybadgerReadApiKey.equals(that.honeybadgerReadApiKey))
            return false;
        if (!feedbackFormDisplayed.equals(that.feedbackFormDisplayed))
            return false;
        return feedbackFormPath.equals(that.feedbackFormPath);

    }

    @Override
    public int hashCode() {
        int result = honeybadgerUrl.hashCode();
        result = 31 * result + apiKey.hashCode();
        result = 31 * result + excludedSysProps.hashCode();
        result = 31 * result + excludedParams.hashCode();
        result = 31 * result + excludedClasses.hashCode();
        result = 31 * result + applicationPackage.hashCode();
        result = 31 * result + honeybadgerReadApiKey.hashCode();
        result = 31 * result + feedbackFormDisplayed.hashCode();
        result = 31 * result + feedbackFormPath.hashCode();
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("BaseChainedConfigContext");
        sb.append("{honeybadgerUrl='").append(honeybadgerUrl).append('\'');
        sb.append(", apiKey='").append(apiKey).append('\'');
        sb.append(", excludedSysProps=").append(excludedSysProps);
        sb.append(", excludedParams=").append(excludedParams);
        sb.append(", excludedClasses=").append(excludedClasses);
        sb.append(", applicationPackage='").append(applicationPackage).append('\'');
        sb.append(", honeybadgerReadApiKey='").append(honeybadgerReadApiKey).append('\'');
        sb.append(", feedbackFormDisplayed=").append(feedbackFormDisplayed);
        sb.append(", feedbackFormPath='").append(feedbackFormPath).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
