package com.nextrade.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EntityScan(basePackages = "com.nextrade.persistence.entity")
@EnableJpaRepositories(basePackages = "com.nextrade.persistence.repository")
@EnableAsync
@EnableScheduling
public class NexTradeApiApplication {
    public static void main(String[] args) {
        SpringApplication.run(NexTradeApiApplication.class, args);
    }
}
