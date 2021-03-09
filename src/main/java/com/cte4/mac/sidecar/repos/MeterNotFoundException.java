package com.cte4.mac.sidecar.repos;

public class MeterNotFoundException extends Exception {

    private static final long serialVersionUID = 1L;

    public MeterNotFoundException() {
        super();
    }

    public MeterNotFoundException(String message) {
        super(message);
    }

    public MeterNotFoundException(Throwable cause) {
        super(cause);
    }

    public MeterNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
    
}
