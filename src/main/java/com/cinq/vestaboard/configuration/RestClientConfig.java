package com.cinq.vestaboard.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
class RestClientConfig {
    @Value("${app.gateway.vestaboard.base-url}")
    private String baseUrl;

    @Bean
    public RestClient restClient() {

        return RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }
}