package com.nextrade.service;

import com.nextrade.common.identifier.Identifier;
import com.nextrade.common.valueobject.Price;
import com.nextrade.contracts.trading.MarketQuote;
import com.nextrade.engine.market.MarketSimulationEngine;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

@Service
public class MarketDataService {

    private final MarketSimulationEngine marketSimulationEngine;
    private final List<Consumer<MarketQuote>> listeners = new CopyOnWriteArrayList<>();

    public MarketDataService(MarketSimulationEngine marketSimulationEngine) {
        this.marketSimulationEngine = marketSimulationEngine;
        marketSimulationEngine.addPriceUpdateListener(this::onPriceUpdate);
    }

    public Price getCurrentPrice(com.nextrade.common.identifier.InstrumentId instrumentId) {
        return marketSimulationEngine.getCurrentPrice(instrumentId);
    }

    public MarketQuote getQuote(com.nextrade.common.identifier.InstrumentId instrumentId) {
        return toContract(marketSimulationEngine.getQuote(instrumentId));
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
        MarketQuote quote = toContract(update.quote());
        if (quote != null) listeners.forEach(listener -> listener.accept(quote));
    }

    public void addQuoteUpdateListener(Consumer<MarketQuote> listener) {
        listeners.add(Objects.requireNonNull(listener, "listener"));
    }

    private static MarketQuote toContract(MarketSimulationEngine.MarketQuote quote) {
        if (quote == null) return null;
        return new MarketQuote(quote.instrumentId().asString(), null, null,
                quote.price().value().toPlainString(), quote.mode(), quote.source(), quote.asOf().toString());
    }
}
