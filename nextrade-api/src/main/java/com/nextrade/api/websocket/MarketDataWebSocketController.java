package com.nextrade.api.websocket;

import com.nextrade.common.identifier.InstrumentId;
import com.nextrade.domain.repository.InstrumentRepository;
import com.nextrade.engine.market.MarketSimulationEngine;
import com.nextrade.service.MarketDataService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

import java.util.Map;
import java.util.UUID;

@Controller
public final class MarketDataWebSocketController {
    private final InstrumentRepository instruments;
    private final MarketDataService marketData;

    public MarketDataWebSocketController(InstrumentRepository instruments, MarketDataService marketData) {
        this.instruments = instruments;
        this.marketData = marketData;
    }

    @MessageMapping("/market.subscribe")
    @SendToUser(value = "/queue/market-subscriptions", broadcast = false)
    public SubscriptionResponse subscribe(Map<String, String> request) {
        if (request == null || request.get("instrumentId") == null) {
            throw new IllegalArgumentException("instrumentId required");
        }
        InstrumentId id;
        try {
            id = InstrumentId.of(UUID.fromString(request.get("instrumentId")));
        } catch (IllegalArgumentException invalidId) {
            throw new IllegalArgumentException("instrumentId must be a UUID");
        }
        var instrument = instruments.findById(id)
                .filter(candidate -> candidate.tradeable())
                .orElseThrow(() -> new IllegalArgumentException("Tradeable instrument not found"));
        var quote = marketData.getQuote(instrument.id());
        return new SubscriptionResponse(id.asString(), "/topic/market/" + id.asString(), quote != null, quote);
    }

    public record SubscriptionResponse(String instrumentId, String topic, boolean quoteAvailable,
                                       MarketSimulationEngine.MarketQuote quote) { }
}
