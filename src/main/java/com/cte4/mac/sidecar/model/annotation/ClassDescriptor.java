package com.cte4.mac.sidecar.model.annotation;

public class ClassDescriptor extends ElementDescriptor {

    private static final long serialVersionUID = 1L;
    private String clazzName;
    private String pkgName;

    public ClassDescriptor(String fullName) {
        super(fullName);
        if (fullName != null) {
            int pos = fullName.trim().lastIndexOf(".");
            this.clazzName = (pos != -1) ? fullName.substring(pos + 1) : fullName;
            this.pkgName = (pos != -1) ? fullName.substring(0, pos) : "";
        }
    }

    public String getClazzName() {
        return this.clazzName;
    }

    public String getPkgName() {
        return pkgName;
    }

    @Override
    public String toString() {
        return String.format("pkgName:%s,clazzName:%s", this.pkgName, this.clazzName);
    }
}
