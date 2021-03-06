package com.cte4.mac.server.utils;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.jboss.byteman.agent.install.Install;
import org.jboss.byteman.agent.install.VMInfo;
import org.springframework.util.DigestUtils;

import lombok.extern.log4j.Log4j2;

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
        int selfPID = Integer.parseInt(ManagementFactory.getRuntimeMXBean().getName().split("@")[0]);
        List<String[]> targets = new ArrayList<String[]>();

        VMInfo[] vms = Install.availableVMs();
        for(VMInfo vm: vms) {
            if(Integer.parseInt(vm.getId()) == selfPID) {
                continue;
            }
            String entryClass = vm.getDisplayName().toLowerCase();
            if(entryClass.indexOf(matcher)!=-1) {
                targets.add(new String[] {vm.getId(), entryClass});
            }
        }
        return targets;
    }

    /**
     * get md5 sign for the given string
     * @param str
     * @return
     */
    public static String getMD5(String str) {
        return DigestUtils.md5DigestAsHex(str.getBytes());
    }

    /**
     * get sorted set
     * @param keys
     * @return
     */
    public static List<String> sortedKeys(Set<String> keys) {
        List<String> orderedKeys = new ArrayList<>();
        orderedKeys.addAll(keys);
        Collections.sort(orderedKeys);
        return orderedKeys;
    }

    public static String loadRuleScript(String ruleFile) {
        String rule = "";
        try {
            URL floc = Optional.ofNullable(ClassLoader.getSystemResource(ruleFile)).orElseThrow(()->new IOException(String.format("unable to find the file:%s", ruleFile)));
            rule = Files.readString(Paths.get(floc.toURI()));
        } catch (Exception e) {
            log.error(String.format("fail to load script from %s", ruleFile), e);
        }
        return rule;
    }

    public static void main(String[] args) {
        getRuntimeVMs("cte4");
    }
}
