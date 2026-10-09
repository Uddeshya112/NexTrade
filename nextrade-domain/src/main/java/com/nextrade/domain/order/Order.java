package com.nextrade.domain.order;

import com.nextrade.common.enumtype.OrderSide;
import com.nextrade.common.enumtype.OrderStatus;
import com.nextrade.common.enumtype.OrderType;
import com.nextrade.common.enumtype.TimeInForce;
import com.nextrade.common.event.OrderCancelledEvent;
import com.nextrade.common.event.OrderExpiredEvent;
import com.nextrade.common.event.OrderRejectedEvent;
import com.nextrade.common.event.OrderTriggeredEvent;
import com.nextrade.common.event.TradeExecutedEvent;
import com.nextrade.common.exception.InvalidOrderException;
import com.nextrade.common.identifier.EventId;
import com.nextrade.common.identifier.OrderId;
import com.nextrade.common.identifier.TradeId;
import com.nextrade.common.valueobject.Price;
import com.nextrade.common.valueobject.Quantity;
import com.nextrade.domain.instrument.Instrument;
import com.nextrade.domain.shared.AbstractAggregateRoot;
import com.nextrade.domain.user.User;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Objects;

/** Order aggregate. Persistence must restore the same public OrderId. */
public final class Order extends AbstractAggregateRoot<OrderId> {
    private final User user;
    private final Instrument instrument;
    private final OrderSide side;
    private OrderType type;
    private OrderStatus status;
    private TimeInForce tif;
    private final Quantity originalQuantity;
    private Quantity workingQuantity;
    private Quantity filledQuantity;
    private Price limitPrice;
    private Price stopPrice;
    private Price avgFillPrice;
    private final String clientOrderId;
    private Instant expiresAt;
    private Instant triggeredAt;
    private long sequence;

    private Order(User user, Instrument instrument, OrderSide side, OrderType type,
                  Quantity quantity, Price limitPrice, Price stopPrice,
                  TimeInForce tif, String clientOrderId) {
        this(OrderId.generate(), user, instrument, side, type, quantity, quantity,
                Quantity.zero(), limitPrice, stopPrice, null, tif, clientOrderId,
                OrderStatus.NEW, null, null, 0L, Instant.now(), Instant.now(), 0L);
        if (!instrument.tradeable()) throw new InvalidOrderException("Instrument is not tradeable");
        if (quantity.isZero()) throw new InvalidOrderException("Quantity must be positive");
        if (type.requiresLimitPrice() && limitPrice == null) throw new InvalidOrderException("Limit price required");
        if (type.requiresStopPrice() && stopPrice == null) throw new InvalidOrderException("Stop price required");
        if (limitPrice != null) validatePositiveTickAlignedPrice(limitPrice, "Limit price");
        if (stopPrice != null) validatePositiveTickAlignedPrice(stopPrice, "Stop price");
    }

    private Order(OrderId id, User user, Instrument instrument, OrderSide side, OrderType type,
                  Quantity originalQuantity, Quantity workingQuantity, Quantity filledQuantity,
                  Price limitPrice, Price stopPrice, Price avgFillPrice, TimeInForce tif,
                  String clientOrderId, OrderStatus status, Instant expiresAt, Instant triggeredAt,
                  long sequence, Instant createdAt, Instant updatedAt, long revision) {
        super(id, createdAt, updatedAt, revision);
        this.user = Objects.requireNonNull(user, "user");
        this.instrument = Objects.requireNonNull(instrument, "instrument");
        this.side = Objects.requireNonNull(side, "side");
        this.type = Objects.requireNonNull(type, "type");
        this.originalQuantity = Objects.requireNonNull(originalQuantity, "originalQuantity");
        this.workingQuantity = Objects.requireNonNull(workingQuantity, "workingQuantity");
        this.filledQuantity = Objects.requireNonNull(filledQuantity, "filledQuantity");
        this.limitPrice = limitPrice;
        this.stopPrice = stopPrice;
        this.avgFillPrice = avgFillPrice;
        this.tif = Objects.requireNonNull(tif, "timeInForce");
        this.clientOrderId = clientOrderId;
        this.status = Objects.requireNonNull(status, "status");
        this.expiresAt = expiresAt;
        this.triggeredAt = triggeredAt;
        this.sequence = sequence;
        if (type.requiresLimitPrice() && limitPrice == null) throw new InvalidOrderException("Limit price required");
        if (type.requiresStopPrice() && stopPrice == null) throw new InvalidOrderException("Stop price required");
        if (limitPrice != null) validatePositiveTickAlignedPrice(limitPrice, "Limit price");
        if (stopPrice != null) validatePositiveTickAlignedPrice(stopPrice, "Stop price");
    }

    public static Order market(User u, Instrument i, OrderSide s, Quantity q, TimeInForce tif, String client) { return new Order(u, i, s, OrderType.MARKET, q, null, null, tif, client); }
    public static Order limit(User u, Instrument i, OrderSide s, Quantity q, Price p, TimeInForce tif, String client) { return new Order(u, i, s, OrderType.LIMIT, q, p, null, tif, client); }
    public static Order stopLoss(User u, Instrument i, OrderSide s, Quantity q, Price stop, TimeInForce tif, String client) { return new Order(u, i, s, OrderType.STOP_LOSS, q, null, stop, tif, client); }
    public static Order stopLimit(User u, Instrument i, OrderSide s, Quantity q, Price p, Price stop, TimeInForce tif, String client) { return new Order(u, i, s, OrderType.STOP_LIMIT, q, p, stop, tif, client); }

    /** Restores persisted state without generating a new identifier or emitting creation events. */
    public static Order reconstitute(OrderId id, User user, Instrument instrument, OrderSide side, OrderType type,
                                     Quantity originalQuantity, Quantity workingQuantity, Quantity filledQuantity,
                                     Price limitPrice, Price stopPrice, Price avgFillPrice, TimeInForce tif,
                                     String clientOrderId, OrderStatus status, Instant expiresAt,
                                     Instant triggeredAt, long sequence, Instant createdAt,
                                     Instant updatedAt, long revision) {
        return new Order(id, user, instrument, side, type, originalQuantity, workingQuantity, filledQuantity,
                limitPrice, stopPrice, avgFillPrice, tif, clientOrderId, status, expiresAt,
                triggeredAt, sequence, createdAt, updatedAt, revision);
    }

    private void validatePositiveTickAlignedPrice(Price price, String label) {
        if (price.value().signum() <= 0) {
            throw new InvalidOrderException(label + " must be positive");
        }
        BigDecimal instrumentTick = instrument.tickSize().value();
        if (instrumentTick.signum() <= 0) {
            throw new InvalidOrderException("Instrument tick size must be positive");
        }
        if (price.value().remainder(instrumentTick).compareTo(BigDecimal.ZERO) != 0) {
            throw new InvalidOrderException(label + " is not aligned with instrument tick size " + instrumentTick);
        }
    }

    public Quantity remaining() { return workingQuantity.subtract(filledQuantity); }
    public boolean active() { return status.isActive(); }
    public boolean marketableAgainst(Price p) {
        return switch (type) {
            case MARKET -> true;
            case LIMIT -> side == OrderSide.BUY ? p.compareTo(limitPrice) <= 0 : p.compareTo(limitPrice) >= 0;
            case STOP_LOSS, STOP_LIMIT -> throw new IllegalStateException("Stop order must trigger before matching");
        };
    }
    public boolean stopTriggered(Price last) {
        if (last == null || !type.isStop()) return false;
        return side == OrderSide.BUY ? last.compareTo(stopPrice) >= 0 : last.compareTo(stopPrice) <= 0;
    }
    public void trigger(Instant now) {
        if (!type.isStop()) return;
        if (status.isTerminal()) throw new IllegalStateException("Terminal order");
        this.type = type == OrderType.STOP_LOSS ? OrderType.MARKET : OrderType.LIMIT;
        triggeredAt = now;
        emit(new OrderTriggeredEvent(EventId.generate(), now, id().asString(), "Stop triggered at " + stopPrice));
        touch();
    }
    public void assignSequence(long seq) { sequence = seq; }
    public long sequence() { return sequence; }
    public void fill(Quantity qty, Price price, TradeId tradeId) {
        if (!active()) throw new IllegalStateException("Inactive order");
        if (qty.isZero() || qty.compareTo(remaining()) > 0) throw new IllegalArgumentException("Invalid fill quantity");
        BigDecimal oldValue = avgFillPrice == null ? BigDecimal.ZERO : avgFillPrice.value().multiply(filledQuantity.value());
        BigDecimal total = filledQuantity.value().add(qty.value());
        avgFillPrice = new Price(oldValue.add(price.value().multiply(qty.value())).divide(total, 8, RoundingMode.HALF_EVEN), price.tickSize());
        filledQuantity = filledQuantity.add(qty);
        status = filledQuantity.compareTo(originalQuantity) >= 0 ? OrderStatus.FILLED : OrderStatus.PARTIALLY_FILLED;
        emit(new TradeExecutedEvent(EventId.generate(), Instant.now(), tradeId.asString(), "Order fill " + id()));
        touch();
    }
    public void cancel(String reason) { if (status.isTerminal()) return; status = OrderStatus.CANCELLED; emit(new OrderCancelledEvent(EventId.generate(), Instant.now(), id().asString(), reason)); touch(); }
    public void reject(String reason) { if (status.isTerminal()) return; status = OrderStatus.REJECTED; emit(new OrderRejectedEvent(EventId.generate(), Instant.now(), id().asString(), reason)); touch(); }
    public void expire() { if (status.isTerminal()) return; status = OrderStatus.EXPIRED; emit(new OrderExpiredEvent(EventId.generate(), Instant.now(), id().asString(), "TIF expired")); touch(); }
    public void modify(Quantity newTotal, Price newLimit) {
        if (!active()) throw new IllegalStateException("Inactive order");
        if (newTotal == null || newTotal.compareTo(filledQuantity) < 0) throw new InvalidOrderException("Quantity below filled amount");
        if (newTotal.compareTo(workingQuantity) > 0) throw new InvalidOrderException("Quantity cannot increase");
        if (type == OrderType.LIMIT && newLimit == null) throw new InvalidOrderException("Limit price required");
        limitPrice = newLimit;
        workingQuantity = newTotal;
        if (filledQuantity.compareTo(newTotal) >= 0) status = OrderStatus.FILLED;
        touch();
    }

    public User user() { return user; }
    public Instrument instrument() { return instrument; }
    public OrderSide side() { return side; }
    public OrderType type() { return type; }
    public OrderStatus status() { return status; }
    public TimeInForce tif() { return tif; }
    public Quantity originalQuantity() { return originalQuantity; }
    public Quantity workingQuantity() { return workingQuantity; }
    public Quantity filledQuantity() { return filledQuantity; }
    public Price limitPrice() { return limitPrice; }
    public Price stopPrice() { return stopPrice; }
    public Price avgFillPrice() { return avgFillPrice; }
    public String clientOrderId() { return clientOrderId; }
    public Instant expiresAt() { return expiresAt; }
    public Instant triggeredAt() { return triggeredAt; }
    public void expiresAt(Instant value) { expiresAt = value; touch(); }
}
