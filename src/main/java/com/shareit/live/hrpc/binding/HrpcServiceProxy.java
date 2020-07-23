package com.shareit.live.hrpc.binding;

import com.shareit.live.hrpc.HrpcClient;
import com.shareit.live.hrpc.annotation.HrpcService;
import com.shareit.live.hrpc.enums.HrpcResponseStatus;
import com.shareit.live.hrpc.enums.SerializeType;
import com.shareit.live.hrpc.exception.HrpcException;
import com.shareit.live.hrpc.service.HrpcRequest;
import com.shareit.live.hrpc.service.HrpcResponse;
import com.shareit.live.hrpc.service.SerializeService;
import com.shareit.live.hrpc.service.impl.ProtostuffSerializeService;
import com.shareit.live.hrpc.util.SerializeUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.BeanFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;

@Slf4j
public class HrpcServiceProxy<T> implements InvocationHandler {

    private final Class<T> hrpcService;

    private final Object invoker;

    private final BeanFactory beanFactory;

    private static final SerializeService defaultSerializeService = new ProtostuffSerializeService();

    public HrpcServiceProxy(Class<T> hrpcService, Object invoker, BeanFactory beanFactory) {
        this.hrpcService = hrpcService;
        this.invoker = invoker;
        this.beanFactory = beanFactory;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        String className = hrpcService.getName();
        String methodName = method.getName();
        if ("toString".equals(methodName) && args.length == 0) {
            return className + "@" + invoker.hashCode();
        } else if ("hashCode".equals(methodName) && args.length == 0) {
            return invoker.hashCode();
        } else if ("equals".equals(methodName) && args.length == 1) {
            return proxy == args[0];
        }

        HrpcService annotation = hrpcService.getAnnotation(HrpcService.class);
        String name = annotation.name();
        String serverFetchBeanName = annotation.serverFetchBeanName();
        SerializeType serializeType = annotation.serialization();

        HrpcRequest hrpcRequest = new HrpcRequest();
        hrpcRequest.setClazz(className);
        hrpcRequest.setMethod(methodName);
        hrpcRequest.setArgs(args);
        hrpcRequest.setArgTypes(method.getParameterTypes());

        SerializeService serializeService = SerializeUtil.getSerializeService(serializeType, defaultSerializeService);
        byte[] req = serializeService.serialize(hrpcRequest);
        ByteBuffer byteBuffer = ByteBuffer.allocate(4 + req.length);
        byteBuffer.putInt(serializeType.getValue());
        byteBuffer.put(req);
        HrpcClient hrpcClient = beanFactory.getBean(HrpcClient.class);
        byte[] rsp = hrpcClient.call(name, serverFetchBeanName, byteBuffer.array());
        if (rsp == null) { //recover method return(e.g. Connection refused)
            return null;
        }
        HrpcResponse hrpcResponse = serializeService.deserializeResponse(rsp, method.getReturnType());

        if (HrpcResponseStatus.ERROR.getCode() == hrpcResponse.getStatus()) {
            Throwable e = hrpcResponse.getE();
            HrpcException exception = new HrpcException(e.getClass().getName() + ": " + e.getMessage(), e);
            StackTraceElement[] stackTrace = e.getStackTrace();
            StackTraceElement[] responseStackTrace = hrpcResponse.getStackTrace();
            StackTraceElement[] allStackTrace = ArrayUtils.addAll(stackTrace, responseStackTrace);
            exception.setStackTrace(allStackTrace);
            throw exception;
        }
        return hrpcResponse.getResult();
    }

}
