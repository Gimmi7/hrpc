package com.shareit.live.hrpc.exception;

public class HrpcException extends RuntimeException {
    /**
     * default constructor
     */
    public HrpcException() {
        super();
    }

    /**
     * Constructor with message & cause
     */
    public HrpcException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructor with message
     */
    public HrpcException(String message) {
        super(message);
    }

    /**
     * Constructor with message format
     */
    public HrpcException(String msgFormat, Object... args) {
        super(String.format(msgFormat, args));
    }

    /**
     * Constructor with cause
     */
    public HrpcException(Throwable cause) {
        super(cause);
    }
}
