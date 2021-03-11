package com.cte4.mac.sidecar.controller;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import com.cte4.mac.sidecar.model.MetricEntity;
import com.cte4.mac.sidecar.model.RuleEntity;
import com.cte4.mac.sidecar.model.TargetEntity;
import com.cte4.mac.sidecar.repos.MetricRepository;
import com.cte4.mac.sidecar.service.AgentAttachException;
import com.cte4.mac.sidecar.service.RuleInjectionException;
import com.cte4.mac.sidecar.service.WeavingService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Api
@RestController
public class AgentController {

    @Autowired
    MetricRepository repo;

    @Autowired
    WeavingService weaving;

    @ApiOperation(value = "list all rules", notes = "here is notes")
    @GetMapping(value = "/rules")
    public List<String> listRules() {
        return repo.getRules().stream().map(t -> t.toString()).collect(Collectors.toList());
    }

    @ApiOperation(value = "list all targets")
    @GetMapping(value = "/targets")
    public List<String> listTargets() {
        return repo.getTargets().stream().map(t -> t.toString()).collect(Collectors.toList());
    }

    @ApiOperation(value = "list all metrics")
    @GetMapping(value = "/metrics")
    public List<String> listMetrics() {
        return repo.getMetrics().stream().map(t -> t.toString()).collect(Collectors.toList());
    }

    @ApiOperation(value = "register target by given PID", notes = "here is notes")
    @GetMapping(value = "/targets/{pid}")
    public TargetEntity registTarget(@PathVariable String pid) {
        log.info("try to register and attach mac agent against PID:" + pid);
        TargetEntity te = repo.getTarget(pid);
        try {
            weaving.attachAgent(te);
        } catch (AgentAttachException e) {
            log.error("fail to register mac for " + te);
        }
        return te;
    }

    @DeleteMapping(value = "/targets/{pid}")
    public TargetEntity delTarget(String pid) {
        return repo.delTarget(pid);
    }

    @ApiOperation(value = "add target attributes", notes = "here is notes")
    @PostMapping(value = "/targets/{pid}")
    public void setTarget(@PathVariable String pid, String attribute, String value) {
        TargetEntity te = repo.getTarget(pid);
        te.getAttributes().put(attribute, value);
    }

    @ApiOperation(value = "apply rule against target process")
    @PostMapping(value = "/rules/{rulename}/{target}")
    public String applyRule(@PathVariable String rulename, @PathVariable String target) {
        TargetEntity te = repo.getTarget(target);
        Optional<RuleEntity> find = repo.getRules().stream().filter(rule -> rule.getName().equals(rulename))
                .findFirst();
        if (find.isPresent()) {
            String returnMsg = String.format("rule:%s applied to target:%s successful", rulename, target);
            RuleEntity re = find.get();
            re = te.addRule(re);
            if (re != null) {
                try {
                    weaving.applyRule(te, re);
                } catch (RuleInjectionException e) {
                    returnMsg = String.format("fail to apply rule:%s against target:%s, error:%s", rulename, target, e);
                }
            } else {
                returnMsg = "rule is already registered";
            }
            return returnMsg;
        }
        return String.format("target[pid:%s] is invalid", target);
    }

    @ApiOperation(value = "detach rule from target process")
    @DeleteMapping(value = "/rules/{rulename}/{target}")
    public String detachRule(@PathVariable String rulename, @PathVariable String target) {
        TargetEntity te = repo.getTarget(target);
        Optional<RuleEntity> find = repo.getRules().stream().filter(rule -> rule.getName().equals(rulename))
                .findFirst();
        if (find.isPresent()) {
            RuleEntity re = find.get();
            if (weaving.detachRule(te, new String[] { re.getName() })) {
                te.delRule(re);
                return "rule detached successfully";
            }
        }
        return "unable to detach the rule (not found or error happened)";
    }

}
