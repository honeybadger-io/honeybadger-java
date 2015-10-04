package io.honeybadger.reporter.dto;

import java.io.Serializable;

/**
 * Class containing the current state of memory on the running JVM.
 *
 * @author <a href="https://github.com/dekobon">Elijah Zupancic</a>
 * @since 1.0.11
 */
public class Memory implements Serializable {
    private static final long serialVersionUID = -8799953046383217102L;

    public final Number total;
    public final Number free;
    public final Number buffers;
    public final Number cached;
    public final Number free_total;

    public Memory() {
        // TODO: Write a Linux /proc/meminfo parser
        final long mebibyte = 1048576L;
        final Runtime r = Runtime.getRuntime();
        this.total = r.maxMemory() / mebibyte;
        this.free = r.freeMemory() / mebibyte;
        this.buffers = -1;
        this.cached = -1;
        this.free_total = (r.maxMemory() - (r.totalMemory() + r.freeMemory())) / mebibyte;
    }

    public Memory(Number total, Number free, Number buffers, Number cached, Number free_total) {
        this.total = total;
        this.free = free;
        this.buffers = buffers;
        this.cached = cached;
        this.free_total = free_total;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Memory memory = (Memory) o;

        if (total != null ? !total.equals(memory.total) : memory.total != null) return false;
        if (free != null ? !free.equals(memory.free) : memory.free != null) return false;
        if (buffers != null ? !buffers.equals(memory.buffers) : memory.buffers != null) return false;
        if (cached != null ? !cached.equals(memory.cached) : memory.cached != null) return false;
        return !(free_total != null ? !free_total.equals(memory.free_total) : memory.free_total != null);

    }

    @Override
    public int hashCode() {
        int result = total != null ? total.hashCode() : 0;
        result = 31 * result + (free != null ? free.hashCode() : 0);
        result = 31 * result + (buffers != null ? buffers.hashCode() : 0);
        result = 31 * result + (cached != null ? cached.hashCode() : 0);
        result = 31 * result + (free_total != null ? free_total.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Memory{" +
                "total=" + total +
                ", free=" + free +
                ", buffers=" + buffers +
                ", cached=" + cached +
                ", free_total=" + free_total +
                '}';
    }
}
