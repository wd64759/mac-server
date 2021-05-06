package com.cte4.mac.server.rule;

import com.cte4.mac.server.model.annotation.ElementDescriptor;

public abstract class RuleTplProcessor {

    public abstract boolean isAccepted(ElementDescriptor elementDescriptor);

    public abstract String generateScript(ElementDescriptor elementDescriptor, ElementDescriptor pDescriptor);

    public abstract String getRuleName();

}
