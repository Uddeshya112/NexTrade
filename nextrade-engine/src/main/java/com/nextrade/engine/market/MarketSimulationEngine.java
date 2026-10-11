package com.nextrade.engine.market;

import com.nextrade.common.identifier.InstrumentId;
import com.nextrade.common.valueobject.Price;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/** In-memory market price source for development and paper-trading use. */
public final class MarketSimulationEngine {
    private final Map<InstrumentId, Price> prices = new ConcurrentHashMap<>();
    private final CopyOnWriteArrayList<Consumer<PriceUpdate>> listeners = new CopyOnWriteArrayList<>();

    public Price getCurrentPrice(InstrumentId instrumentId) {
        return prices.get(instrumentId);
    }

    public void updatePrice(InstrumentId instrumentId, Price price) {
        prices.put(instrumentId, price);
        PriceUpdate update = new PriceUpdate(instrumentId, price);
        listeners.forEach(listener -> listener.accept(update));
    }

    public void addPriceUpdateListener(Consumer<PriceUpdate> listener) {
        listeners.add(listener);
    }

    public record PriceUpdate(InstrumentId instrumentId, Price price) { }
}
