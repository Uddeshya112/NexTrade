package com.nextrade.api.controller;

import com.nextrade.common.identifier.Identifier;
import com.nextrade.common.valueobject.Quantity;
import com.nextrade.common.valueobject.Price;
import com.nextrade.common.enumtype.*;
import com.nextrade.contracts.trading.OrderRequest;
import com.nextrade.contracts.trading.OrderResponse;
import com.nextrade.service.TradingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Tag(name = "Trading", description = "Order management")
@SecurityRequirement(name = "bearerAuth")
public class TradingController {

    private final TradingService tradingService;

    @PostMapping
    @Operation(summary = "Place order")
    public CompletableFuture<ResponseEntity<OrderResponse>> placeOrder(
            @Valid @RequestBody OrderRequest.Place request,
            @AuthenticationPrincipal Jwt jwt) {
        com.nextrade.common.identifier.UserId userId = com.nextrade.common.identifier.UserId.of(UUID.fromString(jwt.getSubject()));
        OrderRequest orderRequest = new OrderRequest(
            request.instrumentId(), request.side(), request.type(), request.quantity(),
            Optional.ofNullable(request.limitPrice()), Optional.ofNullable(request.stopPrice()),
            request.timeInForce() != null ? request.timeInForce() : TimeInForce.DAY);
        return tradingService.placeOrder(orderRequest, userId).thenApply(r -> ResponseEntity.accepted().body(r));
    }

    @PostMapping("/{orderId}/cancel")
    @Operation(summary = "Cancel order")
    public CompletableFuture<ResponseEntity<OrderResponse.CancelResult>> cancelOrder(
            @PathVariable UUID orderId, @AuthenticationPrincipal Jwt jwt) {
        com.nextrade.common.identifier.UserId userId = com.nextrade.common.identifier.UserId.of(UUID.fromString(jwt.getSubject()));
        return tradingService.cancelOrder(com.nextrade.common.identifier.OrderId.of(orderId), userId).thenApply(r -> ResponseEntity.ok(r));
    }

    @PostMapping("/{orderId}/modify")
    @Operation(summary = "Modify order")
    public CompletableFuture<ResponseEntity<OrderResponse>> modifyOrder(
            @PathVariable UUID orderId, @Valid @RequestBody OrderRequest.Modify request,
            @AuthenticationPrincipal Jwt jwt) {
        com.nextrade.common.identifier.UserId userId = com.nextrade.common.identifier.UserId.of(UUID.fromString(jwt.getSubject()));
        // Implementation needed
        return CompletableFuture.completedFuture(ResponseEntity.ok(null));
    }

    @GetMapping
    @Operation(summary = "Order history")
    public ResponseEntity<List<com.nextrade.service.TradingService.OrderSummary>>
    getOrderHistory(@RequestParam(required = false) OrderStatus status,
                    @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "50") int size,
                    @AuthenticationPrincipal Jwt jwt) {
        com.nextrade.common.identifier.UserId userId = com.nextrade.common.identifier.UserId.of(UUID.fromString(jwt.getSubject()));
        return ResponseEntity.ok(tradingService.getOrderHistory(com.nextrade.common.identifier.UserId.of(java.util.UUID.fromString(jwt.getSubject())), status, page, size));
    }

    public record PlaceOrderRequest(UUID instrumentId, OrderSide side, OrderType type, Quantity quantity, Price limitPrice, Price stopPrice, TimeInForce timeInForce) {}
}
