package com.cte4.mac.sidecar.utils;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import lombok.extern.log4j.Log4j2;
import sun.tools.jconsole.LocalVirtualMachine;

@Log4j2
public class MonitorUtil {

    /**
     * Find out all runtime java processes
     * 
     * @param matcher - pattern to match the entry class fullname, it comes from the
     *                module name by default
     * @return
     */
    public static List<String[]> getRuntimeVMs(String matcher) {
        // String component_pattern = "cte4";
        int selfPID = Integer.parseInt(ManagementFactory.getRuntimeMXBean().getName().split("@")[0]);
        List<String[]> targets = new ArrayList<String[]>();
        final Map<Integer, LocalVirtualMachine> vms = LocalVirtualMachine.getAllVirtualMachines();
        for (LocalVirtualMachine vm : vms.values()) {
            // for (Entry<Integer, LocalVirtualMachine> vm : vms.entrySet()) {
            if (vm.vmid() == selfPID) {
                continue;
            }
            String entryClass = vm.displayName().toLowerCase();
            if (entryClass.indexOf(matcher) != -1) {
                targets.add(new String[] { String.valueOf(vm.vmid()), entryClass });
                // log.info(String.format("VM runtime: %s:%s", vm.vmid(), entryClass));
            }
        }
        return targets;
    }
}
