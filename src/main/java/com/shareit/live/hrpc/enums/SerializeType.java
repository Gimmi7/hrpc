package com.shareit.live.hrpc.enums;

import com.shareit.live.hrpc.service.impl.ProtostuffSerializeService;

public enum SerializeType {
    PROTOSTUFF(1, ProtostuffSerializeService.class);

    private int value;
    private Class clazz;

    SerializeType(int value, Class clazz) {
        this.value = value;
        this.clazz = clazz;
    }

    public static SerializeType forValue(int value) {
        for (SerializeType type : SerializeType.values()) {
            if (type.value == value) return type;
        }
        return null;
    }

    public int getValue() {
        return value;
    }

    public Class getClazz() {
        return clazz;
    }
}
