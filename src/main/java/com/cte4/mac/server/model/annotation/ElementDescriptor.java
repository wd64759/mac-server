package com.cte4.mac.server.model.annotation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ElementDescriptor implements Serializable {
    private static final long serialVersionUID = 1L;
    protected String name;
    protected List<AnnotationDescriptor> annotations = new ArrayList<>();
    protected List<ElementDescriptor> children;
    protected transient ElementDescriptor parent;

    public ElementDescriptor(String name) {
        this.name = name;
        this.children = new ArrayList<>();
    }

    public String getName() {
        return this.name;
    }

    public void addChild(ElementDescriptor element) {
        this.children.add(element);
        element.setParent(this);
    }

    public List<ElementDescriptor> getChildren() {
        return this.children;
    }

    public void setParent(ElementDescriptor parent) {
        this.parent = parent;
    }

    public ElementDescriptor getParent() {
        return parent;
    }

    public void addAnnotation(AnnotationDescriptor annotation) {
        this.annotations.add(annotation);
    }

    public List<AnnotationDescriptor> getAnnotations() {
        return this.annotations;
    }

}
