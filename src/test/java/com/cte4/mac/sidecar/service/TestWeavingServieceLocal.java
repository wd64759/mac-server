package com.cte4.mac.sidecar.service;

import com.cte4.mac.sidecar.model.RuleEntity;
import com.cte4.mac.sidecar.model.TargetEntity;
import com.cte4.mac.sidecar.utils.MonitorUtil;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class TestWeavingServieceLocal {

    public static void main(String[] args) throws Exception {
        String pID = "9981";
        String agentPort = "11850";
        // applyHelperJar(pID, agentPort);
        // applyRule("JVM", pID, agentPort);
        detachRule("JVM", pID, agentPort);

        // testRuleApplyAndDetach(pID, agentPort);
    }

    static void applyRule(String ruleName, String processID, String agentPort) throws Exception {
        String ruleFile = String.format("rulescripts/%s.btm", ruleName);
        String ruleScript = MonitorUtil.loadRuleScript(ruleFile);
        TargetEntity te = new TargetEntity(processID);
        te.setAgentPort(agentPort);
        RuleEntity re = new RuleEntity(ruleName);
        re.setScript(ruleScript);

        WeavingService w = new WeavingService();
        w.applyRule(te, re);
        te.getRules().stream().forEach(log::info);

    }

    static void detachRule(String ruleName, String processID, String agentPort) {
        String ruleFile = String.format("rulescripts/%s.btm", ruleName);
        
        RuleEntity re = new RuleEntity(ruleName);
        String ruleScript = MonitorUtil.loadRuleScript(ruleFile);
        re.setScript(ruleScript);

        TargetEntity te = new TargetEntity(processID);
        te.setAgentPort(agentPort);
        te.addRule(re.clone());

        WeavingService w = new WeavingService();
        String[] removeRules = {ruleName};
        w.detachRule(te, removeRules);
        te.getRules().stream().forEach(log::info);
    }

    public static void applyHelperJar(String processID, String port) throws Exception {
        // please replace first 2 params before runing it
        String pID = processID;
        String agentPort = port;
        TargetEntity te = new TargetEntity(pID);
        te.setAgentPort(agentPort);

        WeavingService w = new WeavingService();
        String jarLoc = "/mnt/d/code/e4/machelper/build/libs/machelper-0.0.1.jar";
        w.attachHelpers(te, jarLoc);
    }

    static void testRuleApplyAndDetach(String processID, String port) throws Exception {
        // please replace first 2 params before runing it
        String pID = processID;
        String agentPort = port;
        String ruleFile = "rulescripts/counter.btm";
        String ruleScript = MonitorUtil.loadRuleScript(ruleFile);

        TargetEntity te = new TargetEntity(pID);
        te.setAgentPort(agentPort);

        RuleEntity re = new RuleEntity("TestRule");
        re.setScript(ruleScript);

        WeavingService w = new WeavingService();
        w.applyRule(te, re);
        te.getRules().stream().forEach(log::info);

        System.out.println(">>> on-hold >>>");
        System.in.read();

        String[] removeRules = {"TestRule"};
        w.detachRule(te, removeRules);
        te.getRules().stream().forEach(log::info);
    }

}
