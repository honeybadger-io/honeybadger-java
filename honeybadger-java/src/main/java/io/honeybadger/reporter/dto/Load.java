package io.honeybadger.reporter.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.Serializable;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Scanner;

/**
 * Class containing statistics about the host system's load average.
 *
 * @author <a href="https://github.com/dekobon">Elijah Zupancic</a>
 * @since 1.0.11
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Load implements Serializable {
    private static final long serialVersionUID = 3398000045209329774L;

    private final Number one;
    private final Number five;
    private final Number fifteen;

    public Load() {
        Number[] loadAverages = findLoadAverages();
        this.one = loadAverages[0];
        this.five = loadAverages[1];
        this.fifteen = loadAverages[2];
    }

    @JsonCreator
    public Load(@JsonProperty("one") final Number one,
                @JsonProperty("five") final Number five,
                @JsonProperty("fifteen") final Number fifteen) {
        this.one = one;
        this.five = five;
        this.fifteen = fifteen;
    }

    /**
     * Attempts to find all three load values in a way that is safe for an in-process
     * operation. This leads to platform specific code. We attempt to avoid forking to
     * call external processes because this would be a bad thing to do on every error
     * that came into the system.
     *
     * @return an array containing all three load averages (1, 5, 15) in that order
     */
    static Number[] findLoadAverages() {
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        String os = osBean.getName();

        if (os.equals("Linux")) {
            return findLinuxLoadAverages(osBean);
        } else {
            return defaultLoadAverages(osBean);
        }
    }

    static Number[] findLinuxLoadAverages(final OperatingSystemMXBean osBean) {
        File loadavg = new File("/proc/loadavg");

        if (loadavg.exists() &&  loadavg.isFile() && loadavg.canRead()) {
            try (Scanner scanner = new Scanner(loadavg,
                    StandardCharsets.US_ASCII.name())) {
                if (!scanner.hasNext()) {
                    return defaultLoadAverages(osBean);
                }

                final String line = scanner.nextLine();
                final String[] values = line.split(" ", 4);

                return new Number[]{
                        Double.parseDouble(values[0]),
                        Double.parseDouble(values[1]),
                        Double.parseDouble(values[2])
                };

            } catch (Exception e) {
                LoggerFactory.getLogger(Load.class)
                        .debug("Error reading /proc/loadavg", e);
                return defaultLoadAverages(osBean);
            }
        } else {
            LoggerFactory.getLogger(Load.class)
                    .debug("Couldn't fid or access /proc/loadavg");
            return defaultLoadAverages(osBean);
        }
    }

    static Number[] defaultLoadAverages(final OperatingSystemMXBean osBean) {
        return new Number[] {osBean.getSystemLoadAverage(), null, null };
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || !(o instanceof Load)) {
            return false;
        }

        final Load load = (Load) o;
        return Objects.equals(getOne(), load.getOne()) &&
                Objects.equals(getFive(), load.getFive()) &&
                Objects.equals(getFifteen(), load.getFifteen());
    }

    @Override
    public int hashCode() {

        return Objects.hash(getOne(), getFive(), getFifteen());
    }

    @Override
    public String toString() {
        return "Load{" +
                "one=" + getOne() +
                ", five=" + getFive() +
                ", fifteen=" + getFifteen() +
                '}';
    }

    public Number getOne() {
        return one;
    }

    public Number getFive() {
        return five;
    }

    public Number getFifteen() {
        return fifteen;
    }
}
