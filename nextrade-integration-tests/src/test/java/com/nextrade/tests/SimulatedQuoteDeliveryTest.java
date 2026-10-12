package com.nextrade.tests;

import com.nextrade.api.websocket.MarketDataWebSocketController;
import com.nextrade.api.websocket.MarketQuoteWebSocketPublisher;
import com.nextrade.common.valueobject.Price;
import com.nextrade.domain.repository.InstrumentRepository;
import com.nextrade.engine.market.MarketSimulationEngine;
import com.nextrade.service.MarketDataService;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SimulatedQuoteDeliveryTest {
    @Test
    void websocketSubscriptionReportsNoQuoteUntilSimulatorProvidesOne() {
        var instrument = Fixture.instrument();
        InstrumentRepository repository = mock(InstrumentRepository.class);
        when(repository.findById(instrument.id())).thenReturn(Optional.of(instrument));
        MarketSimulationEngine engine = new MarketSimulationEngine();
        MarketDataService market = new MarketDataService(engine);
        MarketDataWebSocketController controller = new MarketDataWebSocketController(repository, market);

        var empty = controller.subscribe(Map.of("instrumentId", instrument.id().asString()));
        assertFalse(empty.quoteAvailable());
        assertNull(empty.quote());

        engine.updatePrice(instrument.id(), Fixture.price("123.45"));
        var response = controller.subscribe(Map.of("instrumentId", instrument.id().asString()));
        assertTrue(response.quoteAvailable());
        assertEquals("SIMULATED", response.quote().mode());
        assertEquals("NexTrade in-memory simulation engine", response.quote().source());
        assertEquals("123.45", response.quote().last());
        assertNotNull(response.quote().asOf());
    }

    @Test
    void engineUpdatesArePublishedOnlyOnTheInstrumentTopic() {
        var instrument = Fixture.instrument();
        MarketSimulationEngine engine = new MarketSimulationEngine();
        SimpMessagingTemplate messaging = mock(SimpMessagingTemplate.class);
        MarketDataService market = new MarketDataService(engine);
        new MarketQuoteWebSocketPublisher(market, messaging);
        Price price = Fixture.price("99.50");

        engine.updatePrice(instrument.id(), price);

        var payload = org.mockito.ArgumentCaptor.forClass(Object.class);
        verify(messaging).convertAndSend(eq("/topic/market/" + instrument.id().asString()), (Object) payload.capture());
        assertInstanceOf(com.nextrade.contracts.trading.MarketQuote.class, payload.getValue());
        var quote = (com.nextrade.contracts.trading.MarketQuote) payload.getValue();
        assertEquals("SIMULATED", quote.mode());
        assertEquals("99.50", quote.last());
    }
}
