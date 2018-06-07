package io.honeybadger.reporter.dto;

import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.Serializable;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;

import static io.honeybadger.util.HBStringUtils.isPresent;

/**
 * Class containing the current state of memory on the running JVM.
 *
 * @author <a href="https://github.com/dekobon">Elijah Zupancic</a>
 * @since 1.0.11
 */
public class Memory implements Serializable {
    private static final long serialVersionUID = -8799953046383217102L;

    private static final String LINUX_MEMINFO_PATH = "/proc/meminfo";
    public static final int JVM_INFO_INITIAL_CAPACITY = 10;

    public final Number total;
    public final Number free;
    public final Number buffers;
    public final Number cached;
    public final Number free_total;
    public final Number vm_free;
    public final Number vm_max;
    public final Number vm_total;
    public final Number vm_heap;
    public final Number vm_nonheap;

    public Memory() {
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        String os = osBean.getName();

        /* Sadly, we can only get the physical machine values for memory state
         * for Linux only. This is because Linux exposes this information to us
         * as a file so we can safely read it without forking a process. Other
         * OSes require us to fork a process in order to get at this information
         * and it would be dangerous to fork a lot of processes when a large
         * volume of errors was coming in. */
        Map<String, Long> memValues = os.equals("Linux") ?
                findLinuxMemInfo(new File(LINUX_MEMINFO_PATH)) :
                new HashMap<String, Long>();
        Map<String, Number> vmMemValues = findJvmMemInfo();

        this.total = memValues.get("MemTotal");
        this.free = memValues.get("MemFree");
        this.buffers = memValues.get("Buffers");
        this.cached = memValues.get("Cached");
        this.free_total = memValues.get("FreeTotal");
        this.vm_free = vmMemValues.get("VmFreeMem");
        this.vm_max = vmMemValues.get("VmMaxMem");
        this.vm_total = vmMemValues.get("VmTotalMem");
        this.vm_heap = vmMemValues.get("VmHeap");
        this.vm_nonheap = vmMemValues.get("VmNonHeap");
    }

    @SuppressWarnings("CheckStyle")
    public Memory(final Number total, final Number free, final Number buffers, final Number cached,
                  final Number freeTotal, final Number vmFree, final Number vmMax,
                  final Number vmTotal, final Number vmHeap, final Number vmNonheap) {
        this.total = total;
        this.free = free;
        this.buffers = buffers;
        this.cached = cached;
        this.free_total = freeTotal;
        this.vm_free = vmFree;
        this.vm_max = vmMax;
        this.vm_total = vmTotal;
        this.vm_heap = vmHeap;
        this.vm_nonheap = vmNonheap;
    }

    static Map<String, Long> findLinuxMemInfo(final File memInfoFile) {
        final HashMap<String, Long> memInfo = new HashMap<>(50);

        final long mebibyteMultiplier = 1024L;

        if (memInfoFile.exists() && memInfoFile.isFile() && memInfoFile.canRead()) {
            try (Scanner scanner = new Scanner(memInfoFile,
                    StandardCharsets.US_ASCII.name())) {
                while (scanner.hasNext()) {
                    final String line = scanner.nextLine();
                    final String[] fields = line.split("(:?)\\s+", 3);
                    final String name = fields[0];
                    final String kbValue = fields[1];
                    final Long mbValue = Long.parseLong(kbValue) / mebibyteMultiplier;

                    if (!isPresent(name) || !isPresent(kbValue)) {
                        continue;
                    }

                    memInfo.put(name, mbValue);
                }

                final long free = memInfo.getOrDefault("MemFree", 0L);
                final long buffers = memInfo.getOrDefault("Buffers", 0L);
                final long cached = memInfo.getOrDefault("Cached", 0L);

                final long freeTotal = free + buffers + cached;

                memInfo.put("FreeTotal", freeTotal);

            } catch (Exception e) {
                LoggerFactory.getLogger(Memory.class)
                        .error("Error reading memory information", e);
            }
        } else {
            LoggerFactory.getLogger(Memory.class)
                    .warn("Error reading memory information from {}", memInfoFile);
        }

        return memInfo;
    }

    static Map<String, Number> findJvmMemInfo() {
        Map<String, Number> jvmInfo = new HashMap<>(JVM_INFO_INITIAL_CAPACITY);
        Runtime runtime = Runtime.getRuntime();
        MemoryMXBean memBean = ManagementFactory.getMemoryMXBean();
        final long mebibyte = 1_048_576L;

        /* Total amount of free memory available to the JVM */
        jvmInfo.put("VmFreeMem", runtime.freeMemory() / mebibyte);
        /* Maximum amount of memory the JVM will attempt to use */
        jvmInfo.put("VmMaxMem", runtime.maxMemory() / mebibyte);
        /* Total memory currently in use by the JVM */
        jvmInfo.put("VmTotalMem", runtime.totalMemory() / mebibyte);
        jvmInfo.put("VmHeap", memBean.getHeapMemoryUsage().getUsed() / mebibyte);
        jvmInfo.put("VmNonHeap", memBean.getNonHeapMemoryUsage().getUsed() / mebibyte);

        return jvmInfo;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final Memory memory = (Memory) o;
        return Objects.equals(total, memory.total) &&
                Objects.equals(free, memory.free) &&
                Objects.equals(buffers, memory.buffers) &&
                Objects.equals(cached, memory.cached) &&
                Objects.equals(free_total, memory.free_total) &&
                Objects.equals(vm_free, memory.vm_free) &&
                Objects.equals(vm_max, memory.vm_max) &&
                Objects.equals(vm_total, memory.vm_total) &&
                Objects.equals(vm_heap, memory.vm_heap) &&
                Objects.equals(vm_nonheap, memory.vm_nonheap);
    }

    @Override
    public int hashCode() {
        return Objects.hash(total, free, buffers, cached, free_total, vm_free,
                vm_max, vm_total, vm_heap, vm_nonheap);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Memory{");
        sb.append("total=").append(total);
        sb.append(", free=").append(free);
        sb.append(", buffers=").append(buffers);
        sb.append(", cached=").append(cached);
        sb.append(", free_total=").append(free_total);
        sb.append(", vm_free=").append(vm_free);
        sb.append(", vm_max=").append(vm_max);
        sb.append(", vm_total=").append(vm_total);
        sb.append(", vm_heap=").append(vm_heap);
        sb.append(", vm_nonheap=").append(vm_nonheap);
        sb.append('}');
        return sb.toString();
    }
}
