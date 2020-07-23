package com.shareit.live.hrpc.service;

import com.shareit.live.hrpc.enums.HrpcResponseStatus;
import lombok.Data;

@Data
public class HrpcResponse  {

    /**
     * response status
     */
    private int status;

    /**
     * hint message for error
     */
    private String message;

    /**
     * returned result
     */
    private Object result;

    /**
     * exception
     */
    private Throwable e;

    /**
     * stacktrace information for exception
     */
    private StackTraceElement[] stackTrace;

    public void error(String message, Throwable e, StackTraceElement[] stackTrace) {
        this.status = HrpcResponseStatus.ERROR.getCode();
        this.message = message;
        this.e = e;
        this.stackTrace = stackTrace;
    }

    public void success(Object result) {
        this.status = HrpcResponseStatus.SUCCESS.getCode();
        this.result = result;
    }
}
