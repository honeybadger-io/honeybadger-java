package io.honeybadger.reporter.dto;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Server details at the time an error occurred.
 * @author <a href="https://github.com/dekobon">Elijah Zupancic</a>
 * @since 1.0.9
 */
public class ServerDetails implements Serializable {
    private static final long serialVersionUID = 4689643321013504425L;
    private static Logger logger = LoggerFactory.getLogger(ServerDetails.class);

    public final String environment_name;
    public final String hostname;
    public final String project_root;

    public ServerDetails() {
        this.environment_name = environment();
        this.hostname = hostname();
        this.project_root = projectRoot();
    }

    public ServerDetails(String environment_name, String hostname, String project_root) {
        this.environment_name = environment_name;
        this.hostname = hostname;
        this.project_root = project_root;
    }

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ServerDetails that = (ServerDetails) o;

        if (environment_name != null ? !environment_name.equals(that.environment_name) : that.environment_name != null)
            return false;
        if (hostname != null ? !hostname.equals(that.hostname) : that.hostname != null) return false;
        return !(project_root != null ? !project_root.equals(that.project_root) : that.project_root != null);

    }

    @Override
    public int hashCode() {
        int result = environment_name != null ? environment_name.hashCode() : 0;
        result = 31 * result + (hostname != null ? hostname.hashCode() : 0);
        result = 31 * result + (project_root != null ? project_root.hashCode() : 0);
        return result;
    }
}
