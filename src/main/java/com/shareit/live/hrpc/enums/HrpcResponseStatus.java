package com.shareit.live.hrpc.enums;

public enum HrpcResponseStatus {
    SUCCESS(0),
    ERROR(-1);

    private int code;

    HrpcResponseStatus(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
