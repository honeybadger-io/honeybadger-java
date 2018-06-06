package io.honeybadger.reporter.dto;

import java.io.Serializable;
import java.util.Objects;

/**
 * Class containing the statistics about the running JVM.
 *
 * @author <a href="https://github.com/dekobon">Elijah Zupancic</a>
 * @since 1.0.11
 */
public class Stats implements Serializable {
    private static final long serialVersionUID = 4563609532018909058L;

    public final Memory mem;
    public final Load load;

    public Stats() {
        this.mem = new Memory();
        this.load = new Load();
    }

    public Stats(final Memory mem, final Load load) {
        this.mem = mem;
        this.load = load;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Stats stats = (Stats) o;

        if (mem != null ? !mem.equals(stats.mem) : stats.mem != null) return false;
        return !(load != null ? !load.equals(stats.load) : stats.load != null);

    }

    @Override
    public int hashCode() {
        return Objects.hash(mem, load);
    }

    @Override
    public String toString() {
        return "Stats{" +
                "mem=" + mem +
                ", load=" + load +
                '}';
    }
}
