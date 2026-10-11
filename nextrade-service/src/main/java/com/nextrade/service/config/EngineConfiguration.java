package com.nextrade.service.config;

import com.nextrade.engine.market.MarketSimulationEngine;
import com.nextrade.engine.matching.MatchingEngine;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EngineConfiguration {
    @Bean(destroyMethod = "close")
    public MatchingEngine matchingEngine() {
        return new MatchingEngine(4, trade -> { });
    }

    @Bean
    public MarketSimulationEngine marketSimulationEngine() {
        return new MarketSimulationEngine();
    }
}
