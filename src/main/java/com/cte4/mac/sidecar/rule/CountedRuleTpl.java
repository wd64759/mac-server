package com.cte4.mac.sidecar.rule;

import com.cte4.mac.sidecar.model.annotation.ElementDescriptor;
import com.cte4.mac.sidecar.model.annotation.MethodDescriptor;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class CountedRuleTpl extends RuleTplProcessor {

    private String annotationClass = "com.e4.maclient.annotation.Counted";
    private String ruleName = "Counted";

    @Override
    public boolean isAccepted(ElementDescriptor elementDescriptor) {
        if (elementDescriptor instanceof MethodDescriptor && elementDescriptor.getAnnotations().stream()
                .filter(t -> t.getName().equals(annotationClass)).findAny().isPresent()) {
            return true;
        }
        return false;
    }

    @Override
    public String generateScript(ElementDescriptor elementDescriptor, ElementDescriptor pDescriptor) {
        // TODO Auto-generated method stub
        log.info("> TODO: generate rule script **");
        return null;
    }

    @Override
    public String getRuleName() {
        return this.ruleName;
    }

}
