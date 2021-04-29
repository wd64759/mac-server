package com.cte4.mac.sidecar.rule;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import com.cte4.mac.sidecar.model.RuleEntity;
import com.cte4.mac.sidecar.model.annotation.ClassDescriptor;
import com.cte4.mac.sidecar.model.annotation.ElementDescriptor;
import com.cte4.mac.sidecar.model.annotation.MethodDescriptor;
import com.cte4.mac.sidecar.model.annotation.ModuleDescriptor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class RuleManager {

    @Autowired
    public List<RuleTplProcessor> ruleTplRegistery = new ArrayList<>();

    public List<RuleEntity> buildRules(ModuleDescriptor md) {
        List<RuleEntity> rules = new ArrayList<>();
        md.getChildren().forEach(elemDescriptor->{
            // process class level rules
            ClassDescriptor classDescriptor = (ClassDescriptor) elemDescriptor;
            this.ruleTplRegistery.forEach(ruleTpl->{
                if(ruleTpl.isAccepted(classDescriptor)){
                    try {
                        rules.add(generateRule(ruleTpl.getRuleName(), ruleTpl.generateScript(classDescriptor, md)));
                    } catch (Exception e) {
                        log.error("fail to parse annotation as rule script", e);
                    }
                }
            });

            // generate rule for each method
            for(ElementDescriptor eDescriptor: classDescriptor.getChildren()) {
                MethodDescriptor methodDescriptor = (MethodDescriptor) eDescriptor;
                this.ruleTplRegistery.forEach(ruleTpl->{
                    if(ruleTpl.isAccepted(methodDescriptor)){
                        rules.add(generateRule(ruleTpl.getRuleName(), ruleTpl.generateScript(methodDescriptor, classDescriptor)));
                    }
                });
                
            }
        });
        return rules;
    }

    protected RuleEntity generateRule(String ruleName, String script) {
        RuleEntity theRule = new RuleEntity(ruleName);
        theRule.setScript(script);
        return theRule;
    }


    public String getRuleTemplateByAnnotation(String annotationName) {
        return null;
    }


}
