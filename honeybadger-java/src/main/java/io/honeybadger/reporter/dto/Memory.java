package io.honeybadger.reporter.dto;

import com.google.gson.annotations.SerializedName;
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

    private final Number total;
    private final Number free;
    private final Number buffers;
    private final Number cached;
    @SerializedName("vm_freeTotal")
    private final Number freeTotal;
    @SerializedName("vm_free")
    private final Number vmFree;
    @SerializedName("vm_max")
    private final Number vmMax;
    @SerializedName("vm_total")
    private final Number vmTotal;
    @SerializedName("vm_heap")
    private final Number vmHeap;
    @SerializedName("vm_nonheap")
    private final Number vmNonheap;

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
        this.freeTotal = memValues.get("FreeTotal");
        this.vmFree = vmMemValues.get("VmFreeMem");
        this.vmMax = vmMemValues.get("VmMaxMem");
        this.vmTotal = vmMemValues.get("VmTotalMem");
        this.vmHeap = vmMemValues.get("VmHeap");
        this.vmNonheap = vmMemValues.get("VmNonHeap");
    }

    @SuppressWarnings("ParameterNumber")
    // No reason to be restrictive for a DTO
    public Memory(final Number total, final Number free, final Number buffers, final Number cached,
                  final Number freeTotal, final Number vmFree, final Number vmMax,
                  final Number vmTotal, final Number vmHeap, final Number vmNonheap) {
        this.total = total;
        this.free = free;
        this.buffers = buffers;
        this.cached = cached;
        this.freeTotal = freeTotal;
        this.vmFree = vmFree;
        this.vmMax = vmMax;
        this.vmTotal = vmTotal;
        this.vmHeap = vmHeap;
        this.vmNonheap = vmNonheap;
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
        return Objects.equals(getTotal(), memory.getTotal()) &&
                Objects.equals(getFree(), memory.getFree()) &&
                Objects.equals(getBuffers(), memory.getBuffers()) &&
                Objects.equals(getCached(), memory.getCached()) &&
                Objects.equals(getFreeTotal(), memory.getFreeTotal()) &&
                Objects.equals(getVmFree(), memory.getVmFree()) &&
                Objects.equals(getVmMax(), memory.getVmMax()) &&
                Objects.equals(getVmTotal(), memory.getVmTotal()) &&
                Objects.equals(getVmHeap(), memory.getVmHeap()) &&
                Objects.equals(getVmNonheap(), memory.getVmNonheap());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getTotal(), getFree(), getBuffers(), getCached(), getFreeTotal(), getVmFree(),
                getVmMax(), getVmTotal(), getVmHeap(), getVmNonheap());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Memory{");
        sb.append("total=").append(getTotal());
        sb.append(", free=").append(getFree());
        sb.append(", buffers=").append(getBuffers());
        sb.append(", cached=").append(getCached());
        sb.append(", free_total=").append(getFreeTotal());
        sb.append(", vm_free=").append(getVmFree());
        sb.append(", vm_max=").append(getVmMax());
        sb.append(", vm_total=").append(getVmTotal());
        sb.append(", vm_heap=").append(getVmHeap());
        sb.append(", vm_nonheap=").append(getVmNonheap());
        sb.append('}');
        return sb.toString();
    }

    public Number getTotal() {
        return total;
    }

    public Number getFree() {
        return free;
    }

    public Number getBuffers() {
        return buffers;
    }

    public Number getCached() {
        return cached;
    }

    public Number getFreeTotal() {
        return freeTotal;
    }

    public Number getVmFree() {
        return vmFree;
    }

    public Number getVmMax() {
        return vmMax;
    }

    public Number getVmTotal() {
        return vmTotal;
    }

    public Number getVmHeap() {
        return vmHeap;
    }

    public Number getVmNonheap() {
        return vmNonheap;
    }
}
