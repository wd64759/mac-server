package com.cte4.mac.server.service;

import java.util.Properties;

import org.jboss.byteman.agent.submit.Submit;

public class TestSubmit {

    public static void main(String[] args) throws Exception {
        int agentPort = 11390;
        Submit submit = new Submit("localhost", agentPort);
        Properties p = new Properties();
        p.setProperty("org.jboss.byteman.agent_port", "" + agentPort);
        p.setProperty("org.jboss.byteman.debugsss", "false");
        p.setProperty("org.jboss.byteman.verbose", "false");
        
        submit.setSystemProperties(p);
    }
    
}
