package com.shareit.live.hrpc.service.impl;

import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.Hessian2Output;
import com.caucho.hessian.io.SerializerFactory;
import com.shareit.live.hrpc.exception.HrpcException;
import com.shareit.live.hrpc.service.HrpcRequest;
import com.shareit.live.hrpc.service.HrpcResponse;
import com.shareit.live.hrpc.service.SerializeService;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class SofaHessianSerializeService implements SerializeService {

    private SerializerFactory serializerFactory = new SerializerFactory();

    @Override
    public byte[] serialize(HrpcRequest hrpcRequest) {
        return this.encode(hrpcRequest);
    }

    @Override
    public byte[] serialize(HrpcResponse hrpcResponse) {
        return this.encode(hrpcResponse);
    }

    @Override
    public HrpcRequest deserializeRequest(byte[] bytes) {
        return this.decode(bytes);
    }

    @Override
    public HrpcResponse deserializeResponse(byte[] bytes, Class<?> resultType) {
        return this.decode(bytes);
    }

    private byte[] encode(Object object) {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        Hessian2Output hout = new Hessian2Output(bout);
        hout.setSerializerFactory(serializerFactory);
        try {
            hout.writeObject(object);
            hout.close();
            return bout.toByteArray();
        } catch (IOException e) {
            throw new HrpcException("sofa-hessian serialize error:" + e.getMessage());
        }
    }

    private <T> T decode(byte[] data) {
        ByteArrayInputStream bin = new ByteArrayInputStream(data, 0, data.length);
        Hessian2Input hin = new Hessian2Input(bin);
        hin.setSerializerFactory(serializerFactory);
        try {
            T t = (T) hin.readObject();
            hin.close();
            return t;
        } catch (IOException e) {
            throw new HrpcException("sofa-hessian deserialize error:" + e.getMessage());
        }
    }

}
