package com.cte4.mac.server.service;

public class AgentAttachException extends Exception {
    private static final long serialVersionUID = -2361015280220250779L;
    public AgentAttachException() {
    }
    public AgentAttachException(String message) {
        super(message);
    }
    public AgentAttachException(Throwable cause) {
        super(cause);
    }
    public AgentAttachException(String message, Throwable cause) {
        super(message, cause);
    }
}
