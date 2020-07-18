package com.shareit.live.hrpc;

import com.shareit.live.hrpc.annotation.ServerFetch;
import com.shareit.live.hrpc.config.HrpcProperties;
import com.shareit.live.hrpc.config.RemoteServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class HrpcClient {

    private final RestTemplate restTemplate;

    private final BeanFactory beanFactory;

    private final HrpcProperties hrpcProperties;

    private Map<String, String> serverMap = new HashMap<>();


    public HrpcClient(RestTemplate restTemplate, BeanFactory beanFactory, HrpcProperties hrpcProperties) {
        this.restTemplate = restTemplate;
        this.beanFactory = beanFactory;
        this.hrpcProperties = hrpcProperties;
        this.init();
    }

    private void init() {
        List<RemoteServer> remoteServers = hrpcProperties.getRemoteServers();
        if (!CollectionUtils.isEmpty(remoteServers)) {
            for (RemoteServer remoteServer : remoteServers) {
                serverMap.put(remoteServer.getName(), remoteServer.getServer());
            }
        }
        
    }

    @Retryable(value = {RestClientException.class}, maxAttempts = 2, backoff = @Backoff(delay = 200L))
    public byte[] call(String name, String serverFetchBeanName, byte[] req) {
        String server = null;
        if (this.beanFactory.containsBean(serverFetchBeanName)) {
            ServerFetch serverFetch = (ServerFetch) this.beanFactory.getBean(serverFetchBeanName);
            server = serverFetch.fetch(name);
        } else {
            server = this.serverMap.get(name);//todo load balance server list
        }
        if (server.endsWith("/")) {
            server = server.substring(0, server.length() - 1);
        }

        String url = server + "/hrpc";

        byte[] bytes = this.restTemplate.postForObject(url, req, byte[].class);
        return bytes;
    }

    @Recover
    public byte[] recover(RestClientException e, String name, String serverFetchBeanName, byte[] req) {
        log.error("call the recover method when hrpc call {} fail:{}", name, e);
        return null;
    }
}
