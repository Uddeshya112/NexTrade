package com.nextrade.tests;

import com.nextrade.common.identifier.InstrumentId;
import com.nextrade.common.valueobject.Price;
import com.nextrade.domain.instrument.Instrument;
import com.nextrade.domain.repository.InstrumentRepository;
import com.nextrade.engine.market.MarketSimulationEngine;
import com.nextrade.service.config.DemoQuoteBootstrap;
import org.junit.jupiter.api.Test;
import org.springframework.boot.ApplicationArguments;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DemoQuoteBootstrapTest {
    @Test
    void seedsOnlyFixedDemoSymbolsAndMarksTheirQuotesSimulated() {
        Instrument nex = Instrument.create("NEX", "NexTrade Demo", "NSE", "INR", 1, new BigDecimal("0.05"));
        Instrument demo = Instrument.create("DEMO", "Demo Holdings", "NSE", "INR", 1, new BigDecimal("0.05"));
        Instrument external = Instrument.create("REAL", "External instrument", "NSE", "INR", 1, new BigDecimal("0.05"));
        InstrumentRepository instruments = mock(InstrumentRepository.class);
        when(instruments.findActive()).thenReturn(List.of(nex, demo, external));
        MarketSimulationEngine engine = new MarketSimulationEngine();

        new DemoQuoteBootstrap(instruments, engine).run(mock(ApplicationArguments.class));

        assertEquals("100.00", engine.getQuote(nex.id()).price().toString());
        assertEquals("50.00", engine.getQuote(demo.id()).price().toString());
        assertEquals("SIMULATED", engine.getQuote(nex.id()).mode());
        assertTrue(engine.getQuote(nex.id()).source().contains("simulation"));
        assertNull(engine.getQuote(external.id()));
        verify(instruments, times(1)).findActive();
    }
}
