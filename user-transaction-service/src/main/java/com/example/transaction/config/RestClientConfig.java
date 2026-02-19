package com.example.transaction.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.http.client.JdkClientHttpRequestFactory; // Or HttpComponentsClientHttpRequestFactory

import java.time.Duration;

@Configuration
public class RestClientConfig {

    @Bean
    public RestClient restClient(RestClient.Builder builder) {
        return builder
                .requestFactory(getClientHttpRequestFactory())
                .build();
    }

    private JdkClientHttpRequestFactory getClientHttpRequestFactory() {
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory();
        factory.setReadTimeout(Duration.ofSeconds(5));
        // Note: Connect timeout is often handled differently depending on the factory implementation
        return factory;
    }
}