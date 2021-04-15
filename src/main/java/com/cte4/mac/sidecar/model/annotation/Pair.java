package com.cte4.mac.sidecar.model.annotation;

import java.io.Serializable;

public class Pair<T> implements Serializable {
    private static final long serialVersionUID = 1L;
    String key;
    T value;

    public Pair(String key, T v) {
        this.key = key;
        this.value = v;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getKey() {
        return this.key;
    }

    public void setValue(T v) {
        this.value = v;
    }

    public T getValue() {
        return this.value;
    }
}
