package com.cte4.mac.sidecar.service;
import com.sun.tools.attach.VirtualMachine;

public class AttachHelperJar {
    public static void main(String[] args) throws Exception {
        
        VirtualMachine vm = VirtualMachine.attach("29585");
        vm.loadAgent("/mnt/d/code/e4/machelper/build/libs/machelper-0.0.1.jar");
        vm.detach();
    }
}
