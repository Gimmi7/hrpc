package com.shareit.live.hrpc;

import com.shareit.live.hrpc.annotation.ServerFetch;
import com.shareit.live.hrpc.config.HrpcProperties;
import com.shareit.live.hrpc.config.RemoteServer;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Slf4j
public class HrpcClient {

    private final RestTemplate restTemplate;

    private final BeanFactory beanFactory;

    private final HrpcProperties hrpcProperties;

    private Map<String, List<String>> serverMap = new HashMap<>();


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
                String[] serverArr = remoteServer.getServers().split(",");
                List<String> serverList = new ArrayList<>();
                for (String s : serverArr) {
                    serverList.add(s.trim());
                }
                serverMap.put(remoteServer.getName(), serverList);
            }
        }

    }

    @Retryable(value = {RestClientException.class}, maxAttempts = 1, backoff = @Backoff(delay = 200L))
    public byte[] call(String name, String serverFetchBeanName, byte[] req) {
        String server = null;
        if (this.beanFactory.containsBean(serverFetchBeanName)) {
            ServerFetch serverFetch = (ServerFetch) this.beanFactory.getBean(serverFetchBeanName);
            server = serverFetch.fetch(name);
        } else {
            /**
             * simple random load balance strategy
             */
            List<String> serverList = this.serverMap.get(name);
            int random = RandomUtils.nextInt(0, serverList.size());
            server = serverList.get(random);
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
