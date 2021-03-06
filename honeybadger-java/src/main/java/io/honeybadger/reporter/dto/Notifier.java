package io.honeybadger.reporter.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.jcabi.manifests.Manifests;

import java.io.Serializable;
import java.util.Objects;

/**
 * Notifier section of an error reported to the Honeybadger API.
 * @author <a href="https://github.com/dekobon">Elijah Zupancic</a>
 * @since 1.0.9
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Notifier implements Serializable {
    public static final String VERSION;

    static {
        VERSION = findVersion();
    }

    private static final long serialVersionUID = -9160493241433298708L;

    private final String name = "io.honeybadger:honeybadger-java";
    private final String url = "https://github.com/honeybadger-io/honeybadger-java";
    private final String version = VERSION;

    public Notifier() {
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public String getVersion() {
        return version;
    }

    /**
     * Finds the version of the library from the JAR manifest or a system property.
     * @return Sting containing version number or "unknown" if we can't locate the version
     */
    private static String findVersion() {
        final String sysPropVersion = System.getProperty("honeybadger.version");

        if (sysPropVersion != null && !sysPropVersion.isEmpty()) {
            return sysPropVersion;
        }

        try {
            final String manifestVersion = Manifests.read("Honeybadger-Java-Version");

            if (manifestVersion != null && !manifestVersion.isEmpty()) {
                return manifestVersion;
            } else {
                return "unknown";
            }
        } catch (IllegalArgumentException e) {
            // We are probably running in test mode in an IDE
            return "unknown";
        }
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof Notifier)) return false;
        Notifier notifier = (Notifier) o;
        return Objects.equals(name, notifier.name) &&
                Objects.equals(url, notifier.url) &&
                Objects.equals(version, notifier.version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, url, version);
    }
}
