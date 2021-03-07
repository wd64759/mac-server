package com.cte4.mac.sidecar.service;

public class RuleDetachException extends Exception {
    private static final long serialVersionUID = -488583396057448800L;
    public RuleDetachException() {
    }
    public RuleDetachException(String message) {
        super(message);
    }
    public RuleDetachException(String message, Throwable cause) {
        super(message, cause);
    }
}
