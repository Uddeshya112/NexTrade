package com.nextrade.api.controller;

import com.nextrade.common.enumtype.*;
import com.nextrade.contracts.trading.OrderRequest;
import com.nextrade.contracts.trading.ModifyOrderRequest;
import com.nextrade.contracts.trading.OrderResponse;
import com.nextrade.service.TradingService;
import com.nextrade.service.ExecutionQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Tag(name = "Trading", description = "Order management")
@SecurityRequirement(name = "bearerAuth")
public class TradingController {

    private final TradingService tradingService;
    private final ExecutionQueryService executions;

    @PostMapping
    @Operation(summary = "Place order")
    public CompletableFuture<ResponseEntity<OrderResponse>> placeOrder(
            @Valid @RequestBody OrderRequest request,
            @AuthenticationPrincipal String principal) {
        com.nextrade.common.identifier.UserId userId = com.nextrade.common.identifier.UserId.of(UUID.fromString(principal));
        return tradingService.placeOrder(request, userId).thenApply(r -> ResponseEntity.accepted().body(r));
    }

    @PostMapping("/{orderId}/cancel")
    @Operation(summary = "Cancel order")
    public CompletableFuture<ResponseEntity<OrderResponse.CancelResult>> cancelOrder(
            @PathVariable UUID orderId, @AuthenticationPrincipal String principal) {
        com.nextrade.common.identifier.UserId userId = com.nextrade.common.identifier.UserId.of(UUID.fromString(principal));
        return tradingService.cancelOrder(com.nextrade.common.identifier.OrderId.of(orderId), userId).thenApply(r -> ResponseEntity.ok(r));
    }

    @PostMapping("/{orderId}/modify")
    @Operation(summary = "Modify order")
    public CompletableFuture<ResponseEntity<OrderResponse>> modifyOrder(
            @PathVariable UUID orderId, @Valid @RequestBody ModifyOrderRequest request,
            @AuthenticationPrincipal String principal) {
        com.nextrade.common.identifier.UserId userId = com.nextrade.common.identifier.UserId.of(UUID.fromString(principal));
        return tradingService.modifyOrder(orderId, request, userId).thenApply(ResponseEntity::ok);
    }

    @GetMapping
    @Operation(summary = "Order history")
    public ResponseEntity<List<com.nextrade.service.TradingService.OrderSummary>>
    getOrderHistory(@RequestParam(required = false) OrderStatus status,
                    @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "50") int size,
                    @AuthenticationPrincipal String principal) {
        com.nextrade.common.identifier.UserId userId = com.nextrade.common.identifier.UserId.of(UUID.fromString(principal));
        return ResponseEntity.ok(tradingService.getOrderHistory(userId, status, page, size));
    }

    @GetMapping("/{orderId}/executions")
    @Operation(summary = "List persisted executions for an owned order")
    public ResponseEntity<List<com.nextrade.contracts.trading.TradeResponse>> getExecutions(
            @PathVariable UUID orderId, @AuthenticationPrincipal String principal) {
        com.nextrade.common.identifier.UserId userId = com.nextrade.common.identifier.UserId.of(UUID.fromString(principal));
        return ResponseEntity.ok(executions.getExecutions(orderId, userId));
    }

}
