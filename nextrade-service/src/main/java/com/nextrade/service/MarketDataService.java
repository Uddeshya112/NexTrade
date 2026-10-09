package com.nextrade.service;

import com.nextrade.common.identifier.Identifier;
import com.nextrade.common.valueobject.Price;
import com.nextrade.engine.market.MarketSimulationEngine;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CachePut;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class MarketDataService {

    private final MarketSimulationEngine marketSimulationEngine;

    @Cacheable(value = "marketData", key = "#instrumentId.value.toString()")
    public Price getCurrentPrice(Identifier.InstrumentId instrumentId) {
        return marketSimulationEngine.getCurrentPrice(instrumentId);
    }

    public Map<Identifier.InstrumentId, Price> getCurrentPrices(Set<Identifier.InstrumentId> instrumentIds) {
        Map<Identifier.InstrumentId, Price> result = new HashMap<>();
        for (Identifier.InstrumentId id : instrumentIds) {
            Price price = getCurrentPrice(id);
            if (price != null) result.put(id, price);
        }
        return result;
    }

    public void onPriceUpdate(com.nextrade.engine.market.MarketSimulationEngine.PriceUpdate update) {
        // Evict and refresh cache
    }
}
