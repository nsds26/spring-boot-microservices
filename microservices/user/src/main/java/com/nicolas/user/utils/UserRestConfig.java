package com.nicolas.user.utils;

import com.nicolas.user.exception.RestTemplateErrorHandler;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class UserRestConfig {

    @Bean
    @LoadBalanced
    public RestTemplate restTemplate() {
        // Usando RestTemplateBuilder para poder usar o custom error handler RestTemplateErrorHandler:
        return new RestTemplateBuilder().errorHandler(new RestTemplateErrorHandler()).build();
    }
}
