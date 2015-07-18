package io.honeybadger.reporter.dto;

import com.jcabi.manifests.Manifests;

import java.io.Serializable;

/**
 * Notifier section of an error reported to the Honeybadger API.
 */
public class Notifier implements Serializable {
    public static final String VERSION;

    static {
        VERSION = findVersion();
    }

    private static final long serialVersionUID = -9160493241433298708L;

    public final String name = "io.honeybadger:honeybadger-java";
    public final String url = "https://github.com/honeybadger-io/honeybadger-java";
    public final String version = VERSION;

    public Notifier() {
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
}
