package io.honeybadger.reporter.dto;

import io.honeybadger.reporter.config.ConfigContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.TimeZone;

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
    public final Integer pid;
    public final String time;
    public final Stats stats;

    public ServerDetails(final ConfigContext context) {
        this.environment_name = context.getEnvironment();
        this.hostname = hostname();
        this.project_root = projectRoot();
        this.pid = pid();
        this.time = time();
        this.stats = new Stats();
    }

    public ServerDetails(final String environmentName, final String hostname, final String projectRoot,
                         final Integer pid, final String time, final Stats stats) {
        this.environment_name = environmentName;
        this.hostname = hostname;
        this.project_root = projectRoot;
        this.pid = pid;
        this.time = time;
        this.stats = stats;
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

    /**
     * Finds the process id for the running JVM.
     *
     * @see <a href="http://stackoverflow.com/questions/35842/how-can-a-java-program-get-its-own-process-id/7690178#7690178"
     * >referenced this implementation</a>
     * @return process id or null if not found
     */
    protected static Integer pid() {
        // something like '<pid>@<hostname>', at least in SUN / Oracle JVMs
        final String jvmName = ManagementFactory.getRuntimeMXBean().getName();
        final int index = jvmName.indexOf('@');

        if (index < 1) {
            // part before '@' empty (index = 0) / '@' not found (index = -1)
            return null;
        }

        try {
            return Integer.parseInt(jvmName.substring(0, index));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * @return The current time in ISO-8601 format.
     */
    public static String time() {
        TimeZone tz = TimeZone.getTimeZone("UTC");
        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
        formatter.setTimeZone(tz);
        return formatter.format(new Date());
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ServerDetails that = (ServerDetails) o;
        return Objects.equals(environment_name, that.environment_name) &&
                Objects.equals(hostname, that.hostname) &&
                Objects.equals(project_root, that.project_root) &&
                Objects.equals(pid, that.pid) &&
                Objects.equals(time, that.time) &&
                Objects.equals(stats, that.stats);
    }

    @Override
    public int hashCode() {

        return Objects.hash(environment_name, hostname, project_root, pid, time, stats);
    }

    @Override
    public String toString() {
        return "ServerDetails{" +
                "environment_name='" + environment_name + '\'' +
                ", hostname='" + hostname + '\'' +
                ", project_root='" + project_root + '\'' +
                ", pid=" + pid +
                ", stats=" + stats +
                '}';
    }
}
