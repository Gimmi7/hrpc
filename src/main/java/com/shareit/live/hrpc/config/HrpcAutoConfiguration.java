package com.shareit.live.hrpc.config;

import com.shareit.live.hrpc.HrpcClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.web.client.RestTemplate;

@Configuration
@EnableConfigurationProperties(HrpcProperties.class)
@EnableRetry
@Slf4j
public class HrpcAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(RestTemplate.class)
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setReadTimeout(60 * 1000);
        factory.setConnectTimeout(60 * 1000);
        return new RestTemplate(factory);
    }

    @Bean
    @ConditionalOnMissingBean(HrpcClient.class)
    public HrpcClient hrpcClient(RestTemplate restTemplate, HrpcProperties hrpcProperties) {
        return new HrpcClient(restTemplate, BeanFactoryHolder.getBeanFactory(), hrpcProperties);
    }
}
