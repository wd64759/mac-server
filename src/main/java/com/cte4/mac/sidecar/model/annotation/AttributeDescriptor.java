package com.cte4.mac.sidecar.model.annotation;

public class AttributeDescriptor extends ElementDescriptor {
    private static final long serialVersionUID = 1L;
    private String clazzType;

    public AttributeDescriptor(String name) {
        super(name);
    }

    public void setClazzType(String clazzType) {
        this.clazzType = clazzType;
    }

    public String getClazzType() {
        return clazzType;
    }

}
