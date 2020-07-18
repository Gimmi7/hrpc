package com.shareit.live.hrpc;

import com.shareit.live.hrpc.exception.HrpcException;
import com.shareit.live.hrpc.service.HrpcRequest;
import com.shareit.live.hrpc.service.HrpcResponse;
import com.shareit.live.hrpc.service.SerializeService;
import com.shareit.live.hrpc.service.impl.FastjsonSerializeService;
import com.shareit.live.hrpc.util.SerializeUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@ConditionalOnProperty(value = "spring.hrpc.enable", havingValue = "true")
@Slf4j
public class HrpcServer implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    private static final SerializeService defaultSerializeService = new FastjsonSerializeService();


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }


    private Map<Class, Object> serviceBeanMap = new ConcurrentHashMap<>();


    @PostMapping(value = "/hrpc")
    public byte[] hrpc(@RequestBody byte[] bytes) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        int serialize = byteBuffer.getInt();
        byte[] remain = new byte[byteBuffer.remaining()];
        byteBuffer.get(remain);
        SerializeService serializeService = SerializeUtil.getSerializeService(serialize, defaultSerializeService);

        HrpcRequest hrpcRequest = serializeService.deserializeRequest(remain);
        HrpcResponse hrpcResponse = new HrpcResponse();
        try {
            String className = hrpcRequest.getClazz();
            Object bean = this.getBean(Class.forName(className));
            Object[] args = hrpcRequest.getArgs();
            Class[] argsTypes = hrpcRequest.getArgTypes();

            //todo test argsType mismatch
            if (args != null) {
                for (int i = 0; i < args.length; i++) {
                    System.out.println(argsTypes[i].getCanonicalName() + " -> " + args[i].getClass().getCanonicalName());
                }
            }

            Method method = bean.getClass().getMethod(hrpcRequest.getMethod(), argsTypes);
            Object result = method.invoke(bean, args);
            hrpcResponse.success(result);
        } catch (ClassNotFoundException e) {
            String message = e.getClass().getName() + ": " + e.getMessage();
            hrpcResponse.error(message, e, e.getStackTrace());
            log.error("hrpc server, method not implement,className={}", hrpcRequest.getClazz());
        } catch (InvocationTargetException e) {
            String message = e.getClass().getName() + ": " + e.getMessage();
            hrpcResponse.error(message, e, e.getStackTrace());
            log.error("hrpc server method invoke error, className={}, method={}, err={}", hrpcRequest.getClazz(), hrpcRequest.getMethod(), e);
        } catch (IllegalAccessException e) {
            String message = e.getClass().getName() + ": " + e.getMessage();
            hrpcResponse.error(message, e, e.getStackTrace());
            log.error("hrpc server, illegalAccess, className={}, method={}, err={}", hrpcRequest.getClazz(), hrpcRequest.getMethod(), e);
        } catch (NoSuchMethodException e) {
            String message = e.getClass().getName() + ": " + e.getMessage();
            hrpcResponse.error(message, e, e.getStackTrace());
            log.error("hrpc server, no such method err, className={}, method={}, err={}", hrpcRequest.getClazz(), hrpcRequest.getMethod(), e);
        }

        return serializeService.serialize(hrpcResponse);
    }

    private Object getBean(Class clazz) {
        if (serviceBeanMap.containsKey(clazz)) {
            return serviceBeanMap.get(clazz);
        }

        try {
            Map<String, ?> map = applicationContext.getBeansOfType(clazz);
            if (map.size() == 1) {
                Object bean = map.values().iterator().next();
                if (Proxy.isProxyClass(bean.getClass())) {
                    throw new HrpcException("No implement bean class for " + clazz);
                }
                serviceBeanMap.put(clazz, bean);
                return bean;
            } else if (map.size() == 2) {
                Object[] beans = map.values().toArray();
                if (beans[0].getClass().isAssignableFrom(beans[1].getClass())) {
                    serviceBeanMap.put(clazz, beans[1]);
                    return beans[1];
                } else {
                    serviceBeanMap.put(clazz, beans[0]);
                    return beans[0];
                }
            } else if (map.size() == 0) {
                throw new HrpcException("Can not find bean for " + clazz);
            } else {
                throw new HrpcException("Find multi beans " + map.keySet() + " for " + clazz);
            }
        } catch (BeansException e) {
            throw new NoSuchBeanDefinitionException(clazz);
        }
    }

}
