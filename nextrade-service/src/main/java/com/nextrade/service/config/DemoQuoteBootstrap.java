package com.nextrade.service.config;

import com.nextrade.common.valueobject.Price;
import com.nextrade.domain.repository.InstrumentRepository;
import com.nextrade.engine.market.MarketSimulationEngine;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;

/** Seeds fixed, explicitly fictional practice quotes for the two demo-only instruments. */
@Component
public final class DemoQuoteBootstrap implements ApplicationRunner {
    private static final Map<String, String> DEMO_PRICES = Map.of("NEX", "100.00", "DEMO", "50.00");

    private final InstrumentRepository instruments;
    private final MarketSimulationEngine simulation;

    public DemoQuoteBootstrap(InstrumentRepository instruments, MarketSimulationEngine simulation) {
        this.instruments = instruments;
        this.simulation = simulation;
    }

    @Override
    public void run(ApplicationArguments args) {
        instruments.findActive().stream()
                .filter(instrument -> DEMO_PRICES.containsKey(instrument.symbol().value()))
                .forEach(instrument -> {
                    BigDecimal price = new BigDecimal(DEMO_PRICES.get(instrument.symbol().value()));
                    simulation.updatePrice(instrument.id(), Price.of(price, instrument.tickSize().tickSize()));
                });
    }
}
