package io.honeybadger.reporter.dto;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.util.Scanner;

/**
 * Class containing statistics about the host system's load average.
 *
 * @author <a href="https://github.com/dekobon">Elijah Zupancic</a>
 * @since 1.0.11
 */
public class Load implements Serializable {
    private static final long serialVersionUID = 3398000045209329774L;

    public final Number one;
    public final Number five;
    public final Number fifteen;

    public Load() {
        Number[] loadAverages = findLoadAverages();
        this.one = loadAverages[0];
        this.five = loadAverages[1];
        this.fifteen = loadAverages[2];
    }

    public Load(Number one, Number five, Number fifteen) {
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
            File loadavg = new File("/proc/loadavg");

            if (loadavg.exists() &&  loadavg.isFile() && loadavg.canRead()) {
                try (Scanner scanner = new Scanner(loadavg)) {
                    if (!scanner.hasNext()) {
                        return defaultLoadAverages(osBean);
                    }

                    String line = scanner.nextLine();
                    String[] values = line.split(" ", 3);

                    return new Number[]{
                            Double.parseDouble(values[0]),
                            Double.parseDouble(values[1]),
                            Double.parseDouble(values[2])
                    };

                } catch (Exception e) {
                    return defaultLoadAverages(osBean);
                }
            } else {
                return defaultLoadAverages(osBean);
            }

        } else {
            return defaultLoadAverages(osBean);
        }
    }

    static Number[] defaultLoadAverages(OperatingSystemMXBean osBean) {
        return new Number[] { osBean.getSystemLoadAverage(), -1, -1 };
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Load load = (Load) o;

        if (one != null ? !one.equals(load.one) : load.one != null) return false;
        if (five != null ? !five.equals(load.five) : load.five != null) return false;
        return !(fifteen != null ? !fifteen.equals(load.fifteen) : load.fifteen != null);

    }

    @Override
    public int hashCode() {
        int result = one != null ? one.hashCode() : 0;
        result = 31 * result + (five != null ? five.hashCode() : 0);
        result = 31 * result + (fifteen != null ? fifteen.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Load{" +
                "one=" + one +
                ", five=" + five +
                ", fifteen=" + fifteen +
                '}';
    }
}
