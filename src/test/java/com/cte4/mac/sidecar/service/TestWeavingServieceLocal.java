package com.cte4.mac.sidecar.service;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;

import com.cte4.mac.sidecar.model.RuleEntity;
import com.cte4.mac.sidecar.model.TargetEntity;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class TestWeavingServieceLocal {

    public static void main(String[] args) throws Exception {
        String pID = "9054";
        String agentPort = "11667";
        String ruleFile = "rulescripts/counter.btm";
        String ruleScript = loadRuleScript(ruleFile);

        TargetEntity te = new TargetEntity(pID);
        te.setAgentPort(agentPort);

        RuleEntity re = new RuleEntity("TestRule");
        re.setScript(ruleScript);

        WeavingService w = new WeavingService();
        w.applyRule(te, re);
        te.getRules().stream().forEach(log::info);

        String[] removeRules = {"TestRule"};
        w.detachRule(te, removeRules);
        te.getRules().stream().forEach(log::info);
    }

    static String loadRuleScript(String ruleFile) {
        String rule = "";
        try {
            URL floc = Optional.ofNullable(ClassLoader.getSystemResource(ruleFile)).orElseThrow(()->new IOException(String.format("unable to find the file:%s", ruleFile)));
            rule = Files.readString(Paths.get(floc.toURI()));
        } catch (Exception e) {
            log.error(String.format("fail to load script from %s", ruleFile), e);
        }
        return rule;
    }

}
