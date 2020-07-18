package com.shareit.live.hrpc.service;

public interface SerializeService {

    byte[] serialize(HrpcRequest hrpcRequest);

    byte[] serialize(HrpcResponse hrpcResponse);

    HrpcRequest deserializeRequest(byte[] bytes);

    HrpcResponse deserializeResponse(byte[] bytes, Class<?> resultType);
}
