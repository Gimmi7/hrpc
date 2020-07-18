package com.shareit.live.hrpc.util;

import com.shareit.live.hrpc.enums.SerializeType;
import com.shareit.live.hrpc.service.SerializeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.EnumUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class SerializeUtil {
    private static final Map<Integer, SerializeService> cacheMap = new ConcurrentHashMap<>();

    public static SerializeService getSerializeService(SerializeType serializeType, SerializeService defaultSerializeService) {
        if (EnumUtils.isValidEnum(SerializeType.class, serializeType.toString())) {
            int value = serializeType.getValue();
            SerializeService cachedSerializeService = cacheMap.get(value);
            if (cachedSerializeService != null) {
                return cachedSerializeService;
            } else {
                try {
                    cachedSerializeService = (SerializeService) serializeType.getClazz().newInstance();
                    cacheMap.put(value, cachedSerializeService);
                } catch (InstantiationException | IllegalAccessException e) {
                    log.error("{} newInstance err, use default serializeService", serializeType.getClazz().getCanonicalName());
                    cachedSerializeService = defaultSerializeService;
                }
                return cachedSerializeService;
            }
        } else {
            return defaultSerializeService;
        }
    }

    public static SerializeService getSerializeService(int value, SerializeService defaultSerializeService) {
        SerializeType serializeType = SerializeType.forValue(value);
        return getSerializeService(serializeType, defaultSerializeService);
    }
}
