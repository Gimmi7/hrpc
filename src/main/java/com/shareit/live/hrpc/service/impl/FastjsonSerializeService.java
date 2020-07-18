package com.shareit.live.hrpc.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.Feature;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.shareit.live.hrpc.service.HrpcRequest;
import com.shareit.live.hrpc.service.HrpcResponse;
import com.shareit.live.hrpc.service.SerializeService;

public class FastjsonSerializeService implements SerializeService {

    /**
     * need open autoType
     *
     * @param hrpcRequest
     * @return
     */
    @Override
    public byte[] serialize(HrpcRequest hrpcRequest) {
        return JSON.toJSONBytes(hrpcRequest, SerializerFeature.WriteClassName);
    }

    @Override
    public byte[] serialize(HrpcResponse hrpcResponse) {
        return JSON.toJSONBytes(hrpcResponse);
    }

    @Override
    public HrpcRequest deserializeRequest(byte[] bytes) {
        return JSON.parseObject(bytes, HrpcRequest.class, Feature.OrderedField);
    }

    @Override
    public HrpcResponse deserializeResponse(byte[] bytes, Class<?> resultType) {
        HrpcResponse hrpcResponse = JSON.parseObject(bytes, HrpcResponse.class, Feature.OrderedField);
        if (!resultType.equals(void.class) && hrpcResponse.getResult() != null) {
            Object result = JSON.parseObject(JSON.toJSONString(hrpcResponse.getResult()), resultType);
            hrpcResponse.setResult(result);
        }
        return hrpcResponse;
    }
}
