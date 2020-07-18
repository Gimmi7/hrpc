package com.shareit.live.hrpc.config;

import lombok.Data;

@Data
public class RemoteServer {

    /**
     * remote server name
     */
    private String name;

    /**
     * comma separated server address
     */
    private String server;
}
