package com.nextrade.api.websocket;

import com.nextrade.service.MarketDataService;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public final class MarketQuoteWebSocketPublisher {
    public MarketQuoteWebSocketPublisher(MarketDataService marketData, SimpMessagingTemplate messaging) {
        marketData.addQuoteUpdateListener(quote -> messaging.convertAndSend(
                "/topic/market/" + quote.instrumentId(), quote));
    }
}
