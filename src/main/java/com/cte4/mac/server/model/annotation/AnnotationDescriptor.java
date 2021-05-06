package com.cte4.mac.server.model.annotation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class AnnotationDescriptor implements Serializable {
    private static final long serialVersionUID = 1L;
    private String name;
    private List<Pair<?>> values;

    public AnnotationDescriptor(String name) {
        this.name = name;
        this.values = new ArrayList<>();
    }

    public void setValue(Pair<?> pairValue) {
        this.values.add(pairValue);
    }

    public List<Pair<?>> getValue() {
        return this.values;
    }

    public String getName() {
        return this.name;
    }
}
