package io.honeybadger.reporter.dto;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Server details at the time an error occurred.
 * @author <a href="https://github.com/dekobon">Elijah Zupancic</a>
 * @since 1.0.9
 */
public class ServerDetails {
    private static Logger logger = LoggerFactory.getLogger(ServerDetails.class);

    public final String environment_name = environment();
    public final String hostname = hostname();
    public final String project_root = projectRoot();

    /**
     * Finds the name of the environment by looking at a few common Java
     * system properties and/or environment variables.
     *
     * @return the name of the environment, otherwise "development"
     */
    protected static String environment() {
        String hbEnv = System.getenv("HONEYBADGER_ENV");
        if (hbEnv != null && !hbEnv.isEmpty()) return hbEnv;

        String sysPropJavaEnv = System.getProperty("JAVA_ENV");
        if (sysPropJavaEnv != null && !sysPropJavaEnv.isEmpty()) return sysPropJavaEnv;

        String javaEnv = System.getenv("JAVA_ENV");
        if (javaEnv != null && !javaEnv.isEmpty()) return javaEnv;

        String sysPropEnv = System.getProperty("ENV");
        if (sysPropEnv != null && !sysPropEnv.isEmpty()) return sysPropEnv;

        String env = System.getenv("ENV");
        if (env != null && !env.isEmpty()) return env;

        // If no system property defined, then return development
        return "development";
    }

    /**
     * Attempt to find the hostname of the system reporting the error to
     * Honeybadger.
     *
     * @return the hostname of the system reporting the error, "unknown" if not found
     */
    protected static String hostname() {
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

    /**
     * Finds the directory in which the JVM was started.
     *
     * @return the filesystem root in which the project is running
     */
    protected static String projectRoot() {
        try {
            return (new File(".")).getCanonicalPath();
        } catch (IOException e) {
            logger.error("Can't get runtime root path", e);
            return "unknown";
        }
    }
}
