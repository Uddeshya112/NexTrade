package com.nextrade.api.config;

import com.nextrade.service.InMemoryRateLimitStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ServiceConfiguration {
    @Bean
    public InMemoryRateLimitStore rateLimitStore() {
        return new InMemoryRateLimitStore();
    }
}
