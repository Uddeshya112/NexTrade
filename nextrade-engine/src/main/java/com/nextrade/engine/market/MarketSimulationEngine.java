package com.nextrade.engine.market;

import com.nextrade.common.identifier.InstrumentId;
import com.nextrade.common.valueobject.Price;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.time.Instant;
import java.util.function.Consumer;

/** In-memory market price source for development and paper-trading use. */
public final class MarketSimulationEngine {
    private final Map<InstrumentId, MarketQuote> prices = new ConcurrentHashMap<>();
    private final CopyOnWriteArrayList<Consumer<PriceUpdate>> listeners = new CopyOnWriteArrayList<>();

    public Price getCurrentPrice(InstrumentId instrumentId) {
        MarketQuote quote = prices.get(instrumentId);
        return quote == null ? null : quote.price();
    }

    public MarketQuote getQuote(InstrumentId instrumentId) {
        return prices.get(instrumentId);
    }

    public void updatePrice(InstrumentId instrumentId, Price price) {
        MarketQuote quote = new MarketQuote(instrumentId, price, "SIMULATED",
                "NexTrade in-memory simulation engine", Instant.now());
        prices.put(instrumentId, quote);
        PriceUpdate update = new PriceUpdate(quote);
        listeners.forEach(listener -> listener.accept(update));
    }

    public void addPriceUpdateListener(Consumer<PriceUpdate> listener) {
        listeners.add(listener);
    }

    public record MarketQuote(InstrumentId instrumentId, Price price, String mode, String source, Instant asOf) { }
    public record PriceUpdate(MarketQuote quote) {
        public InstrumentId instrumentId() { return quote.instrumentId(); }
        public Price price() { return quote.price(); }
    }
}
