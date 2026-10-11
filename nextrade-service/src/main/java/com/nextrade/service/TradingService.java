package com.nextrade.service;

import com.nextrade.common.enumtype.OrderSide;
import com.nextrade.common.enumtype.OrderStatus;
import com.nextrade.common.enumtype.OrderType;
import com.nextrade.common.enumtype.TimeInForce;
import com.nextrade.common.identifier.InstrumentId;
import com.nextrade.common.identifier.OrderId;
import com.nextrade.common.identifier.UserId;
import com.nextrade.common.valueobject.Price;
import com.nextrade.common.valueobject.Quantity;
import com.nextrade.contracts.trading.ModifyOrderRequest;
import com.nextrade.contracts.trading.OrderRequest;
import com.nextrade.contracts.trading.OrderResponse;
import com.nextrade.domain.instrument.Instrument;
import com.nextrade.domain.order.Order;
import com.nextrade.domain.repository.InstrumentRepository;
import com.nextrade.domain.repository.OrderRepository;
import com.nextrade.domain.repository.UserRepository;
import com.nextrade.domain.user.User;
import com.nextrade.engine.matching.MatchingEngine;
import com.nextrade.engine.matching.MatchingResult;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/** Paper-trading order orchestration. Cash and position reservation must be supplied by account repositories before live trading. */
@Service
public class TradingService {
    private final OrderRepository orders;
    private final InstrumentRepository instruments;
    private final UserRepository users;
    private final MatchingEngine matching;
    private final MarketDataService marketData;

    public TradingService(OrderRepository orders, InstrumentRepository instruments, UserRepository users,
                          MatchingEngine matching, MarketDataService marketData) {
        this.orders = orders;
        this.instruments = instruments;
        this.users = users;
        this.matching = matching;
        this.marketData = marketData;
    }

    public CompletableFuture<OrderResponse> placeOrder(OrderRequest request, UserId userId) {
        try {
            User user = users.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));
            if (!user.loginAllowed(Instant.now())) throw new SecurityException("User is inactive, unverified, or locked");
            InstrumentId instrumentId = InstrumentId.of(UUID.fromString(request.instrumentId()));
            Instrument instrument = instruments.findById(instrumentId).orElseThrow(() -> new IllegalArgumentException("Instrument not found"));
            if (!instrument.tradeable()) throw new IllegalStateException("Instrument is not tradeable");
            OrderSide side = OrderSide.valueOf(request.side().toUpperCase(Locale.ROOT));
            OrderType type = OrderType.valueOf(request.type().toUpperCase(Locale.ROOT));
            TimeInForce tif = request.timeInForce() == null || request.timeInForce().isBlank()
                    ? TimeInForce.DAY : TimeInForce.valueOf(request.timeInForce().toUpperCase(Locale.ROOT));
            Quantity quantity = Quantity.of(request.quantity());
            Price limit = parsePrice(request.limitPrice(), instrument);
            Price stop = parsePrice(request.stopPrice(), instrument);
            if (type.requiresLimitPrice() && limit == null) throw new IllegalArgumentException("Limit price is required");
            if (type.requiresStopPrice() && stop == null) throw new IllegalArgumentException("Stop price is required");
            if (type == OrderType.MARKET && marketData.getCurrentPrice(instrumentId) == null)
                throw new IllegalStateException("No market quote is available for this instrument");
            Order order = switch (type) {
                case MARKET -> Order.market(user, instrument, side, quantity, tif, request.clientOrderId());
                case LIMIT -> Order.limit(user, instrument, side, quantity, limit, tif, request.clientOrderId());
                case STOP_LOSS -> Order.stopLoss(user, instrument, side, quantity, stop, tif, request.clientOrderId());
                case STOP_LIMIT -> Order.stopLimit(user, instrument, side, quantity, limit, stop, tif, request.clientOrderId());
            };
            orders.save(order);
            return matching.place(order).thenApply(result -> {
                orders.save(order);
                return result.accepted()
                        ? OrderResponse.placed(result.orderId(), result.finalStatus(), result.trades())
                        : OrderResponse.rejected(result.orderId(), result.rejectionReason());
            });
        } catch (RuntimeException error) {
            return CompletableFuture.failedFuture(error);
        }
    }

    public CompletableFuture<OrderResponse.CancelResult> cancelOrder(OrderId orderId, UserId userId) {
        Order order = orders.findById(orderId).orElseThrow(() -> new IllegalArgumentException("Order not found"));
        if (!order.user().id().equals(userId)) throw new SecurityException("Order belongs to another user");
        if (!order.status().isActive()) return CompletableFuture.completedFuture(OrderResponse.CancelResult.alreadyTerminal(order.status()));
        return matching.cancel(order.instrument().id(), orderId).thenApply(cancelled -> {
            if (!cancelled) return OrderResponse.CancelResult.notFound(orderId);
            order.cancel("User cancellation");
            orders.save(order);
            return OrderResponse.CancelResult.cancelled(orderId);
        });
    }

    public CompletableFuture<OrderResponse> modifyOrder(UUID id, ModifyOrderRequest request, UserId userId) {
        OrderId orderId = OrderId.of(id);
        Order order = orders.findById(orderId).orElseThrow(() -> new IllegalArgumentException("Order not found"));
        if (!order.user().id().equals(userId)) throw new SecurityException("Order belongs to another user");
        Quantity quantity = Quantity.of(request.quantity());
        Price limit = parsePrice(request.limitPrice(), order.instrument());
        order.modify(quantity, limit);
        orders.save(order);
        return CompletableFuture.completedFuture(OrderResponse.placed(orderId, order.status(), List.of()));
    }

    public List<OrderSummary> getOrderHistory(UserId userId, OrderStatus status, int page, int size) {
        if (page < 0 || size < 1 || size > 200) throw new IllegalArgumentException("Invalid pagination bounds");
        return orders.findByUserIdAndStatus(userId, status).stream().skip((long) page * size).limit(size)
                .map(order -> new OrderSummary(order.id().asString(), order.instrument().symbol().value(),
                        order.side(), order.type(), order.status(), order.originalQuantity().toString(),
                        order.filledQuantity().toString(), order.limitPrice() == null ? null : order.limitPrice().toString(),
                        order.createdAt().toString())).toList();
    }

    public PortfolioSummary getPortfolioSummary(UserId userId) {
        throw new IllegalStateException("Portfolio persistence and cash/position accounting are not implemented yet");
    }

    private static Price parsePrice(String value, Instrument instrument) {
        if (value == null || value.isBlank()) return null;
        return new Price(new BigDecimal(value), instrument.tickSize().tickSize());
    }

    public record OrderSummary(String orderId, String symbol, OrderSide side, OrderType type, OrderStatus status,
                               String quantity, String filledQuantity, String limitPrice, String createdAt) { }
    public record PortfolioSummary(String message) { }
}
