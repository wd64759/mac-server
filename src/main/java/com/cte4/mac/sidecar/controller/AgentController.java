package com.cte4.mac.sidecar.controller;

import java.util.List;
import java.util.Optional;

import com.cte4.mac.sidecar.model.MetricEntity;
import com.cte4.mac.sidecar.model.RuleEntity;
import com.cte4.mac.sidecar.model.TargetEntity;
import com.cte4.mac.sidecar.repos.MetricRepository;
import com.cte4.mac.sidecar.service.AgentAttachException;
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
    public List<RuleEntity> listRules() {
        return repo.getRules();
    }

    @ApiOperation(value = "list all targets")
    @GetMapping(value = "/targets")
    public List<TargetEntity> listTargets() {
        return repo.getTargets();
    }

    @ApiOperation(value = "list all metrics")
    @GetMapping(value = "/metrics")
    public List<MetricEntity> listMetrics() {
        return repo.getMetrics();
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
    public void applyRule(@PathVariable String rulename, @PathVariable String target) {
        TargetEntity te = repo.getTarget(target);
        Optional<RuleEntity> find = repo.getRules().stream().filter(rule -> rule.getName().equals(rulename))
                .findFirst();
        if (find.isPresent()) {
            RuleEntity re = find.get();
            te.applyRule(re);
        }
    }

    @ApiOperation(value = "detach rule from target process")
    @DeleteMapping(value = "/rules/{rulename}/{target}")
    public void detachRule(@PathVariable String rulename, @PathVariable String target) {
        TargetEntity te = repo.getTarget(target);
        Optional<RuleEntity> find = repo.getRules().stream().filter(rule -> rule.getName().equals(rulename))
                .findFirst();
        if (find.isPresent()) {
            RuleEntity re = find.get();
            if (weaving.detachRule(te, new String[] { re.getName() })) {
                te.delRule(re);
            }
        }
    }

}
