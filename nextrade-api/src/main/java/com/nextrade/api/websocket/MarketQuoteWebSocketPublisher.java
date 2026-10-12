package com.nextrade.api.websocket;

import com.nextrade.engine.market.MarketSimulationEngine;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public final class MarketQuoteWebSocketPublisher {
    public MarketQuoteWebSocketPublisher(MarketSimulationEngine engine, SimpMessagingTemplate messaging) {
        engine.addPriceUpdateListener(update -> messaging.convertAndSend(
                "/topic/market/" + update.instrumentId().asString(), update.quote()));
    }
}
