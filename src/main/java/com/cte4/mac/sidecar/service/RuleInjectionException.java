package com.cte4.mac.sidecar.service;

public class RuleInjectionException extends Exception {
    private static final long serialVersionUID = 1L;
    public RuleInjectionException() {
    }
    public RuleInjectionException(String message) {
        super(message);
    }
    public RuleInjectionException(String message, Throwable cause) {
        super(message, cause);
    }
    public RuleInjectionException(Throwable e) {
        super(e);
    }
}
