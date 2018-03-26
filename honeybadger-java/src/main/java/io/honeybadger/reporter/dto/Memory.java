package io.honeybadger.reporter.dto;

import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.Serializable;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.util.HashMap;
import java.util.Map;
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

    public Memory(Number total, Number free, Number buffers, Number cached,
                  Number free_total, Number vm_free, Number vm_max,
                  Number vm_total, Number vm_heap, Number vm_nonheap) {
        this.total = total;
        this.free = free;
        this.buffers = buffers;
        this.cached = cached;
        this.free_total = free_total;
        this.vm_free = vm_free;
        this.vm_max = vm_max;
        this.vm_total = vm_total;
        this.vm_heap = vm_heap;
        this.vm_nonheap = vm_nonheap;
    }

    static Map<String, Long> findLinuxMemInfo(File memInfoFile) {
        HashMap<String, Long> memInfo = new HashMap<>(50);

        final long mebibyteMultiplier = 1024L;

        if (memInfoFile.exists() && memInfoFile.isFile() && memInfoFile.canRead()) {
            try (Scanner scanner = new Scanner(memInfoFile)) {
                while (scanner.hasNext()) {
                    String line = scanner.nextLine();
                    String[] fields = line.split("(:?)\\s+", 3);
                    String name = fields[0];
                    String kbValue = fields[1];
                    Long mbValue = Long.parseLong(kbValue) / mebibyteMultiplier;

                    if (!isPresent(name) || !isPresent(kbValue)) continue;

                    memInfo.put(name, mbValue);
                }

                long free = memInfo.containsKey("MemFree") ?
                        memInfo.get("MemFree") : 0L;
                long buffers = memInfo.containsKey("Buffers") ?
                        memInfo.get("Buffers") : 0L;
                long cached = memInfo.containsKey("Cached") ?
                        memInfo.get("Cached") : 0L;

                long freeTotal = free + buffers + cached;

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
        Map<String, Number> jvmInfo = new HashMap<>(10);
        Runtime runtime = Runtime.getRuntime();
        MemoryMXBean memBean = ManagementFactory.getMemoryMXBean();
        final long mebibyte = 1048576L;

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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Memory memory = (Memory) o;

        if (total != null ? !total.equals(memory.total) : memory.total != null)
            return false;
        if (free != null ? !free.equals(memory.free) : memory.free != null)
            return false;
        if (buffers != null ? !buffers.equals(memory.buffers) : memory.buffers != null)
            return false;
        if (cached != null ? !cached.equals(memory.cached) : memory.cached != null)
            return false;
        if (free_total != null ? !free_total.equals(memory.free_total) : memory.free_total != null)
            return false;
        if (vm_free != null ? !vm_free.equals(memory.vm_free) : memory.vm_free != null)
            return false;
        if (vm_max != null ? !vm_max.equals(memory.vm_max) : memory.vm_max != null)
            return false;
        if (vm_total != null ? !vm_total.equals(memory.vm_total) : memory.vm_total != null)
            return false;
        if (vm_heap != null ? !vm_heap.equals(memory.vm_heap) : memory.vm_heap != null)
            return false;
        return !(vm_nonheap != null ? !vm_nonheap.equals(memory.vm_nonheap) : memory.vm_nonheap != null);

    }

    @Override
    public int hashCode() {
        int result = total != null ? total.hashCode() : 0;
        result = 31 * result + (free != null ? free.hashCode() : 0);
        result = 31 * result + (buffers != null ? buffers.hashCode() : 0);
        result = 31 * result + (cached != null ? cached.hashCode() : 0);
        result = 31 * result + (free_total != null ? free_total.hashCode() : 0);
        result = 31 * result + (vm_free != null ? vm_free.hashCode() : 0);
        result = 31 * result + (vm_max != null ? vm_max.hashCode() : 0);
        result = 31 * result + (vm_total != null ? vm_total.hashCode() : 0);
        result = 31 * result + (vm_heap != null ? vm_heap.hashCode() : 0);
        result = 31 * result + (vm_nonheap != null ? vm_nonheap.hashCode() : 0);
        return result;
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
