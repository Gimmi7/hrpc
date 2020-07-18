package com.shareit.live.hrpc.service.impl;

import com.shareit.live.hrpc.service.HrpcRequest;
import com.shareit.live.hrpc.service.HrpcResponse;
import com.shareit.live.hrpc.service.SerializeService;
import com.shareit.live.hrpc.util.ProtobufUtil;

public class ProtostuffSerializeService implements SerializeService {
    @Override
    public byte[] serialize(HrpcRequest hrpcRequest) {
        return ProtobufUtil.serialize(hrpcRequest);
    }

    @Override
    public byte[] serialize(HrpcResponse hrpcResponse) {
        return ProtobufUtil.serialize(hrpcResponse);
    }

    @Override
    public HrpcRequest deserializeRequest(byte[] bytes) {
        return ProtobufUtil.deserialize(bytes, HrpcRequest.class);
    }

    @Override
    public HrpcResponse deserializeResponse(byte[] bytes, Class<?> resultType) {
        return ProtobufUtil.deserialize(bytes, HrpcResponse.class);
    }
}
