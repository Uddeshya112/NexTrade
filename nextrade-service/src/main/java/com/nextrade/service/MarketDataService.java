package com.nextrade.service;

import com.nextrade.common.identifier.Identifier;
import com.nextrade.common.valueobject.Price;
import com.nextrade.engine.market.MarketSimulationEngine;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class MarketDataService {

    private final MarketSimulationEngine marketSimulationEngine;

    public Price getCurrentPrice(com.nextrade.common.identifier.InstrumentId instrumentId) {
        return marketSimulationEngine.getCurrentPrice(instrumentId);
    }

    public MarketSimulationEngine.MarketQuote getQuote(com.nextrade.common.identifier.InstrumentId instrumentId) {
        return marketSimulationEngine.getQuote(instrumentId);
    }

    public Map<com.nextrade.common.identifier.InstrumentId, Price> getCurrentPrices(Set<com.nextrade.common.identifier.InstrumentId> instrumentIds) {
        Map<com.nextrade.common.identifier.InstrumentId, Price> result = new HashMap<>();
        for (com.nextrade.common.identifier.InstrumentId id : instrumentIds) {
            Price price = getCurrentPrice(id);
            if (price != null) result.put(id, price);
        }
        return result;
    }

    public void onPriceUpdate(com.nextrade.engine.market.MarketSimulationEngine.PriceUpdate update) {
        // Quote reads are served directly from the simulator's timestamped snapshot.
    }
}
