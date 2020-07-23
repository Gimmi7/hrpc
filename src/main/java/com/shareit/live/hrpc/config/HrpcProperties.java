package com.shareit.live.hrpc.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@Data
@ConfigurationProperties(prefix = "hrpc")
public class HrpcProperties {
    /**
     * enable hrpc server launch
     */
    private boolean enable;

    /**
     * server listen port
     */
    private int port;

    /**
     * client config
     */
    private List<RemoteServer> remoteServers;

}
