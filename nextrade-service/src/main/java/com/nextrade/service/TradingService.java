package com.nextrade.service;

import com.nextrade.common.identifier.*;
import com.nextrade.common.valueobject.*;
import com.nextrade.common.enumtype.*;
import com.nextrade.common.exception.*;
import com.nextrade.contracts.trading.OrderRequest;
import com.nextrade.contracts.trading.OrderResponse;
import com.nextrade.domain.order.Order;
import com.nextrade.domain.order.Trade;
import com.nextrade.domain.instrument.Instrument;
import com.nextrade.domain.user.User;
import com.nextrade.domain.user.Wallet;
import com.nextrade.domain.portfolio.Portfolio;
import com.nextrade.domain.repository.*;
import com.nextrade.engine.matching.MatchingEngine;
import com.nextrade.engine.risk.RiskEngine;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class TradingService {

    private final OrderRepository orderRepository;
    private final TradeRepository tradeRepository;
    private final InstrumentRepository instrumentRepository;
    private final UserRepository userRepository;
    private final PortfolioRepository portfolioRepository;
    private final WalletRepository walletRepository;
    private final MatchingEngine matchingEngine;
    private final RiskEngine riskEngine;
    private final MarketDataService marketDataService;
    private final LedgerService ledgerService;
    private final OutboxEventPublisher outboxPublisher;

    @Transactional
    public CompletableFuture<OrderResponse> placeOrder(OrderRequest request, com.nextrade.common.identifier.UserId userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));
        if (!user.isActive()) throw new IllegalStateException("User account not active");

        Instrument instrument = instrumentRepository.findById(request.instrumentId()).orElseThrow(() -> new IllegalArgumentException("Instrument not found"));
        if (!instrument.isTradeable()) throw new IllegalStateException("Instrument not tradeable");

        validateOrderRequest(request, instrument);

        Portfolio portfolio = portfolioRepository.findByUserId(userId).orElseThrow(() -> new IllegalStateException("Portfolio not found"));
        Wallet wallet = walletRepository.findByUserId(userId).orElseThrow(() -> new IllegalStateException("Wallet not found"));

        Map<com.nextrade.common.identifier.InstrumentId, Price> currentPrices = marketDataService.getCurrentPrices(Set.of(request.instrumentId()));
        RiskEngine.RiskContext riskCtx = new RiskEngine.RiskContext(
            userId, request.instrumentId(), request.side(), request.type(),
            request.quantity(), request.limitPrice(), request.stopPrice(),
            portfolio, wallet, currentPrices, riskEngine.getConfig()
        );

        RiskEngine.RiskResult riskResult = riskEngine.evaluate(riskCtx);
        if (!riskResult.passed()) {
            log.warn("Risk check failed for user {}: {}", userId, riskResult.violation().get().message());
            return CompletableFuture.completedFuture(OrderResponse.rejected(request.instrumentId(), riskResult.violation().get().message()));
        }

        // DURABLE RESERVATION BEFORE ENGINE SUBMISSION
        if (request.side() == OrderSide.BUY) {
            Price price = request.limitPrice().orElse(currentPrices.get(request.instrumentId()));
            Money orderValue = Money.of(price.getValue().multiply(request.quantity().getValue()), wallet.getCurrency());
            wallet.reserveCash(orderValue);
        } else {
            Optional<com.nextrade.domain.portfolio.Holding> holdingOpt = portfolio.getHolding(request.instrumentId());
            if (holdingOpt.isEmpty() || holdingOpt.get().getAvailableQuantity().compareTo(request.quantity()) < 0) {
                throw new InsufficientFundsException("Insufficient available shares for sell order");
            }
            holdingOpt.get().reserveQuantity(request.quantity());
        }

        // SAVE RESERVATIONS + ORDER ATOMICALLY
        Order order = createDomainOrder(user, instrument, request);
        orderRepository.save(order);
        walletRepository.save(wallet);
        if (request.side() == OrderSide.SELL) portfolioRepository.save(portfolio);

        // PUBLISH TO OUTBOX FOR DURABLE ENGINE SUBMISSION
        OutboxEvent event = OutboxEvent.orderPlaced(order, request);
        outboxPublisher.publish(event);

        // RETURN PENDING RESPONSE IMMEDIATELY
        return CompletableFuture.completedFuture(
            OrderResponse.pending(order.getId(), request.clientOrderId(), order.getStatus(), List.of())
        );
    }

    @Transactional
    public CompletableFuture<OrderResponse.CancelResult> cancelOrder(com.nextrade.common.identifier.OrderId orderId, com.nextrade.common.identifier.UserId userId) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new OrderNotFoundException("Order not found: " + orderId));
        if (!order.getUser().getId().equals(userId)) throw new SecurityException("Unauthorized");
        if (order.getStatus().isTerminal()) return CompletableFuture.completedFuture(OrderResponse.CancelResult.alreadyTerminal(order.getStatus()));

        MatchingEngine.OrderCommand.Cancel command = new MatchingEngine.OrderCommand.Cancel(
            userId, order.getInstrument().getId(), orderId, new CompletableFuture<>());
        return matchingEngine.submit(command).thenApply(result -> {
            if (result instanceof OrderResponse.Cancelled cancelled) {
                onOrderCancelled(cancelled.orderId(), userId, "User cancel");
            }
            return mapCancelResult(result, order);
        });
    }

    @Transactional
    public void onOrderCancelled(com.nextrade.common.identifier.OrderId orderId, com.nextrade.common.identifier.UserId userId, String reason) {
        Order order = orderRepository.findById(orderId).orElseThrow();
        Wallet wallet = walletRepository.findByUserId(userId).orElseThrow();
        if (order.getSide() == OrderSide.BUY) {
            Price reservationPrice = order.getReservationPrice().orElseThrow();
            Money reservedValue = Money.of(reservationPrice.getValue().multiply(order.getRemainingQuantity().getValue()), wallet.getCurrency());
            wallet.releaseCashReservation(reservedValue);
        } else {
            Portfolio portfolio = portfolioRepository.findByUserId(userId).orElseThrow();
            com.nextrade.domain.portfolio.Holding holding = portfolio.getHolding(order.getInstrument().getId()).orElseThrow();
            holding.releaseQuantityReservation(order.getRemainingQuantity());
            portfolioRepository.save(portfolio);
        }
        walletRepository.save(wallet);
        orderRepository.save(order);
    }

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void expireDayOrders() {
        List<Order> expired = orderRepository.findExpiredDayOrders(Instant.now());
        for (Order order : expired) {
            CompletableFuture<OrderResponse> future = matchingEngine.submit(
                new MatchingEngine.OrderCommand.Cancel(
                    order.getUser().getId(), 
                    order.getInstrument().getId(), 
                    order.getId(), 
                    new CompletableFuture<>()
                )
            );
            
            try {
                OrderResponse result = future.get(5, TimeUnit.SECONDS);
                if (result instanceof OrderResponse.Cancelled) {
                    onOrderCancelled(order.getId(), order.getUser().getId(), "DAY order expired");
                }
            } catch (TimeoutException e) {
                log.warn("Timeout cancelling expired DAY order: {}", order.getId());
            } catch (Exception e) {
                log.error("Error cancelling expired order: {}", order.getId(), e);
            }
        }
    }

    @Transactional
    public List<com.nextrade.service.TradingService.OrderSummary> getOrderHistory(com.nextrade.common.identifier.UserId userId, OrderStatus status, int page, int size) {
        List<Order> orders = orderRepository.findByUserIdAndStatus(userId, status);
        return orders.stream().skip((long) page * size).limit(size).map(this::toSummary).toList();
    }

    @Transactional
    public com.nextrade.service.TradingService.PortfolioSummary getPortfolioSummary(com.nextrade.common.identifier.UserId userId) {
        Portfolio portfolio = portfolioRepository.findByUserId(userId).orElseThrow(() -> new IllegalStateException("Portfolio not found"));
        Wallet wallet = walletRepository.findByUserId(userId).orElseThrow(() -> new IllegalStateException("Wallet not found"));
        Map<com.nextrade.common.identifier.InstrumentId, Price> prices = marketDataService.getCurrentPrices(portfolio.getHoldings().stream().map(Holding::getInstrument).map(Instrument::getId).toList());
        Money totalEquity = portfolio.getTotalEquity(prices);
        Money unrealizedPnl = portfolio.getUnrealizedPnl(prices);
        Money availableCash = wallet.getAvailableBalance();

        List<com.nextrade.service.TradingService.HoldingSummary> holdings = portfolio.getHoldings().stream()
                .map(h -> toHoldingSummary(h, prices.get(h.getInstrument().getId()))).toList();
        return new com.nextrade.service.TradingService.PortfolioSummary(totalEquity, availableCash,
            wallet.getReservedBalance(), portfolio.getTotalRealizedPnl(), unrealizedPnl, holdings);
    }

    private void validateOrderRequest(OrderRequest request, Instrument instrument) {
        if (request.quantity().isZero()) throw new InvalidOrderException("Quantity must be positive");
        if (request.type().requiresLimitPrice() && request.limitPrice().isEmpty()) throw new InvalidOrderException("Limit price required for " + request.type());
        if (request.type().requiresStopPrice() && request.stopPrice().isEmpty()) throw new InvalidOrderException("Stop price required for " + request.type());
        request.limitPrice().ifPresent(price -> { if (!price.equals(price.roundToTick())) throw new InvalidOrderException("Price not aligned with tick size"); });
        request.stopPrice().ifPresent(price -> { if (!price.equals(price.roundToTick())) throw new InvalidOrderException("Stop price not aligned with tick size"); });
    }

    private Order createDomainOrder(User user, Instrument instrument, OrderRequest request) {
        return switch (request.type()) {
            case MARKET -> Order.createMarket(user, instrument, request.side(), request.quantity(), request.timeInForce());
            case LIMIT -> Order.createLimit(user, instrument, request.side(), request.quantity(), request.limitPrice().get(), request.timeInForce());
            case STOP_LOSS -> Order.createStopLoss(user, instrument, request.side(), request.quantity(), request.stopPrice().get(), request.timeInForce());
            case STOP_LIMIT -> Order.createStopLimit(user, instrument, request.side(), request.quantity(), request.limitPrice().get(), request.stopPrice().get(), request.timeInForce());
        };
    }

    private OrderResponse mapResult(MatchingEngine.OrderResult result, Order order) {
        return switch (result) {
            case MatchingEngine.OrderResponse.Placed(var orderId, var status, var trades) -> OrderResponse.placed(orderId, status, trades);
            case MatchingEngine.OrderResponse.OrderRejected(var orderId, var reason) -> OrderResponse.rejected(orderId, reason);
            case MatchingEngine.OrderResponse.OrderNotFound(var orderId) -> OrderResponse.rejected(orderId, "Not found in matching engine");
            case MatchingEngine.OrderResponse.OrderCancelled(var orderId) -> OrderResponse.cancelled(orderId);
        };
    }

    private OrderResponse.CancelResult mapCancelResult(MatchingEngine.OrderResult result, Order order) {
        return switch (result) {
            case MatchingEngine.OrderResponse.OrderCancelled(var orderId) -> OrderResponse.CancelResult.cancelled(orderId);
            case MatchingEngine.OrderResponse.OrderNotFound(var orderId) -> OrderResponse.CancelResult.notFound(orderId);
            default -> OrderResponse.CancelResult.failed("Unexpected: " + result);
        };
    }

    public record OrderRequest(com.nextrade.common.identifier.InstrumentId instrumentId, OrderSide side, OrderType type, Quantity quantity, Optional<Price> limitPrice, Optional<Price> stopPrice, TimeInForce timeInForce) {}

    public record OrderResult(boolean success, com.nextrade.common.identifier.OrderId orderId, OrderStatus status, List<MatchingEngine.TradeExecution> trades, String errorMessage) {
        public static OrderResponse placed(com.nextrade.common.identifier.OrderId orderId, OrderStatus status, List<MatchingEngine.TradeExecution> trades) { return new OrderResponse(true, orderId, status, trades, null); }
        public static OrderResponse rejected(com.nextrade.common.identifier.OrderId orderId, String error) { return new OrderResponse(false, orderId, OrderStatus.REJECTED, List.of(), error); }
        public static OrderResponse cancelled(com.nextrade.common.identifier.OrderId orderId) { return new OrderResponse(true, orderId, OrderStatus.CANCELLED, List.of(), null); }
        public static OrderResponse pending(com.nextrade.common.identifier.OrderId orderId, String clientOrderId, OrderStatus status, List<MatchingEngine.TradeExecution> trades) { return new OrderResponse(true, orderId, status, trades, null); }
    }

    public record CancelResult(boolean success, com.nextrade.common.identifier.OrderId orderId, String message) {
        public static CancelResult success(com.nextrade.common.identifier.OrderId orderId) { return new CancelResult(true, orderId, "Cancelled"); }
        public static CancelResult notFound(com.nextrade.common.identifier.OrderId orderId) { return new CancelResult(false, orderId, "Not found"); }
        public static CancelResult alreadyTerminal(OrderStatus status) { return new CancelResult(false, null, "Already " + status); }
        public static CancelResult failed(String msg) { return new CancelResult(false, null, msg); }
    }

    public record OrderSummary(com.nextrade.common.identifier.OrderId orderId, String symbol, OrderSide side, OrderType type, OrderStatus status, Quantity quantity, Quantity filledQuantity, Optional<Price> limitPrice, Optional<Price> avgFillPrice, Instant createdAt) {}
    public record PortfolioSummary(Money totalEquity, Money availableCash, Money reservedCash, Money realizedPnl, Money unrealizedPnl, List<HoldingSummary> holdings) {}
    public record HoldingSummary(com.nextrade.common.identifier.InstrumentId instrumentId, String symbol, String name, Quantity quantity, Price avgBuyPrice, Price currentPrice, Money unrealizedPnl, Money realizedPnl) {}

    private OrderSummary toSummary(Order order) { return new OrderSummary(order.getId(), order.getInstrument().getSymbol().getValue(), order.getSide(), order.getType(), order.getStatus(), order.getQuantity(), order.getFilledQuantity(), order.getLimitPrice(), order.getAvgFillPrice(), order.getCreatedAt()); }
    private HoldingSummary toHoldingSummary(com.nextrade.domain.portfolio.Holding holding, Price currentPrice) { Money unrealized = currentPrice != null ? holding.getUnrealizedPnl(currentPrice) : Money.zero(); return new HoldingSummary(holding.getInstrument().getId(), holding.getInstrument().getSymbol().getValue(), holding.getInstrument().getName(), holding.getQuantity(), holding.getAvgBuyPrice(), currentPrice != null ? currentPrice : Price.of(java.math.BigDecimal.ZERO), unrealized, holding.getRealizedPnl()); }
}
