package io.honeybadger.reporter.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
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
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"environment_name", "hostname", "project_root", "pid", "time", "stats", ""})
public class ServerDetails implements Serializable {
    private static final long serialVersionUID = 4689643321013504425L;
    private static Logger logger = LoggerFactory.getLogger(ServerDetails.class);
    @JsonProperty("environment_name")
    private final String environmentName;
    private final String hostname;
    @JsonProperty("project_root")
    private final String projectRoot;
    private final Integer pid;
    private final String time;
    private final Stats stats;

    public ServerDetails(final ConfigContext context) {
        this.environmentName = context.getEnvironment();
        this.hostname = hostname();
        this.projectRoot = projectRoot();
        this.pid = pid();
        this.time = time();
        this.stats = new Stats();
    }

    @JsonCreator
    public ServerDetails(@JsonProperty("environment_name") final String environmentName,
                         @JsonProperty("hostname") final String hostname,
                         @JsonProperty("project_root") final String projectRoot,
                         @JsonProperty("pid") final Integer pid,
                         @JsonProperty("time") final String time,
                         @JsonProperty("stats") final Stats stats) {
        this.environmentName = environmentName;
        this.hostname = hostname;
        this.projectRoot = projectRoot;
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
        return Objects.equals(getEnvironmentName(), that.getEnvironmentName()) &&
                Objects.equals(getHostname(), that.getHostname()) &&
                Objects.equals(getProjectRoot(), that.getProjectRoot()) &&
                Objects.equals(getPid(), that.getPid()) &&
                Objects.equals(getTime(), that.getTime()) &&
                Objects.equals(getStats(), that.getStats());
    }

    @Override
    public int hashCode() {

        return Objects.hash(getEnvironmentName(), getHostname(), getProjectRoot(), getPid(), getTime(), getStats());
    }

    @Override
    public String toString() {
        return "ServerDetails{" +
                "environment_name='" + getEnvironmentName() + '\'' +
                ", hostname='" + getHostname() + '\'' +
                ", project_root='" + getProjectRoot() + '\'' +
                ", pid=" + getPid() +
                ", stats=" + getStats() +
                '}';
    }

    public String getEnvironmentName() {
        return environmentName;
    }

    public String getHostname() {
        return hostname;
    }

    public String getProjectRoot() {
        return projectRoot;
    }

    public Integer getPid() {
        return pid;
    }

    public String getTime() {
        return time;
    }

    public Stats getStats() {
        return stats;
    }
}
