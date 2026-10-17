package com.nextrade.service.config;

import com.nextrade.engine.market.MarketSimulationEngine;
import com.nextrade.engine.matching.MatchingEngine;
import com.nextrade.service.SettlementService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EngineConfiguration {
    @Bean(destroyMethod = "close")
    public MatchingEngine matchingEngine(SettlementService settlements) {
        return new MatchingEngine(4, settlements::settle);
    }

    @Bean
    public MarketSimulationEngine marketSimulationEngine() {
        return new MarketSimulationEngine();
    }
}
