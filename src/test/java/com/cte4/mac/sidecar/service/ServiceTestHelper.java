package com.cte4.mac.sidecar.service;

import java.io.IOException;
import java.util.Collections;
import java.util.Properties;

import org.assertj.core.util.Arrays;
import org.jboss.byteman.agent.install.Install;
import org.jboss.byteman.agent.install.VMInfo;

// import com.sun.tools.attach.VirtualMachine;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class ServiceTestHelper {
    public static void main(String[] args) throws Exception {
        VMInfo[] vms = Install.availableVMs();
        
        Arrays.asList(vms).stream().forEach(log::info);
        // VirtualMachine vm = VirtualMachine.attach("2287");
        // log.info(props);
        // boolean s = Install.isAgentAttached("2287");
        // log.info(s);
    }
}
