package com.shareit.live.hrpc.service;

import lombok.Data;

@Data
public class HrpcRequest {

    /**
     * implemented interface
     */
    private String clazz;

    /**
     * method name
     */
    private String method;

    /**
     * args of method
     */
    private Object[] args;

    /**
     * arg types
     */
    private Class<?>[] argTypes;

}
