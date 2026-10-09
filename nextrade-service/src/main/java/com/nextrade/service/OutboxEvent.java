package com.nextrade.service;

import com.nextrade.common.identifier.Identifier;
import com.nextrade.common.valueobject.Price;
import com.nextrade.common.valueobject.Quantity;
import com.nextrade.common.enumtype.*;
import com.nextrade.contracts.trading.OrderRequest;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type", visible = true)
@JsonSubTypes({
    @JsonSubTypes.Type(value = OutboxEvent.OrderPlaced.class, name = "order_placed"),
    @JsonSubTypes.Type(value = OutboxEvent.TradeExecuted.class, name = "trade_executed"),
    @JsonSubTypes.Type(value = OutboxEvent.OrderCancelled.class, name = "order_cancelled"),
    @JsonSubTypes.Type(value = OutboxEvent.EmailVerificationSent.class, name = "email_verification_sent"),
    @JsonSubTypes.Type(value = OutboxEvent.PasswordResetSent.class, name = "password_reset_sent"),
    @JsonSubTypes.Type(value = OutboxEvent.LoginAlert.class, name = "login_alert"),
    @JsonSubTypes.Type(value = OutboxEvent.SecurityAlert.class, name = "security_alert"),
    @JsonSubTypes.Type(value = OutboxEvent.WelcomeEmail.class, name = "welcome_email"),
    @JsonSubTypes.Type(value = OutboxEvent.StopCascadeLimitReached.class, name = "stop_cascade_limit_reached")
})
public sealed interface OutboxEvent permits OutboxEvent.OrderPlaced, OutboxEvent.TradeExecuted, OutboxEvent.OrderCancelled, OutboxEvent.EmailVerificationSent, OutboxEvent.PasswordResetSent, OutboxEvent.LoginAlert, OutboxEvent.SecurityAlert, OutboxEvent.WelcomeEmail, OutboxEvent.StopCascadeLimitReached {
    String getType();

    record OrderPlaced(String type, UUID eventId, UUID aggregateId, String payload, Instant createdAt) implements OutboxEvent {
        public OrderPlaced(Order order, OrderRequest request) {
            this("order_placed", UUID.randomUUID(), order.getId().getValue(), 
                toJson(new OrderPlacedPayload(order, request)), Instant.now());
        }

        record OrderPlacedPayload(Order order, OrderRequest request) {}
    }

    record TradeExecuted(String type, UUID eventId, UUID aggregateId, String payload, Instant createdAt) implements OutboxEvent {
        public TradeExecuted(OrderBook.TradeExecution execution) {
            this("trade_executed", UUID.randomUUID(), execution.instrumentId().getValue(),
                toJson(new TradeExecutedPayload(execution)), Instant.now());
        }

        record TradeExecutedPayload(Identifier.TradeId tradeId, Identifier.OrderId buyOrderId, Identifier.OrderId sellOrderId,
                                   Identifier.UserId buyUserId, Identifier.UserId sellUserId,
                                   Identifier.InstrumentId instrumentId, Quantity quantity, Price price, OrderType aggressorType, Instant timestamp) {}
    }

    record OrderCancelled(String type, UUID eventId, UUID aggregateId, String payload, Instant createdAt) implements OutboxEvent {
        public OrderCancelled(Identifier.OrderId orderId, String reason, CancelSource source) {
            this("order_cancelled", UUID.randomUUID(), orderId.getValue(),
                toJson(new OrderCancelledPayload(orderId, reason, source)), Instant.now());
        }

        record OrderCancelledPayload(Identifier.OrderId orderId, String reason, CancelSource source) {}
    }

    record EmailVerificationSent(String type, UUID eventId, UUID aggregateId, String payload, Instant createdAt) implements OutboxEvent {
        public EmailVerificationSent(String email, String code, String username) {
            this("email_verification_sent", UUID.randomUUID(), UUID.randomUUID(),
                toJson(new EmailVerificationPayload(email, code, username)), Instant.now());
        }
        record EmailVerificationPayload(String email, String code, String username) {}
    }

    record PasswordResetSent(String type, UUID eventId, UUID aggregateId, String payload, Instant createdAt) implements OutboxEvent {
        public PasswordResetSent(String email, String token, String username) {
            this("password_reset_sent", UUID.randomUUID(), UUID.randomUUID(),
                toJson(new PasswordResetPayload(email, token, username)), Instant.now());
        }
        record PasswordResetPayload(String email, String token, String username) {}
    }

    record LoginAlert(String type, UUID eventId, UUID aggregateId, String payload, Instant createdAt) implements OutboxEvent {
        public LoginAlert(String email, String ip, String userAgent) {
            this("login_alert", UUID.randomUUID(), UUID.randomUUID(),
                toJson(new LoginAlertPayload(email, ip, userAgent)), Instant.now());
        }
        record LoginAlertPayload(String email, String ip, String userAgent) {}
    }

    record SecurityAlert(String type, UUID eventId, UUID aggregateId, String payload, Instant createdAt) implements OutboxEvent {
        public SecurityAlert(String email, String event, String details) {
            this("security_alert", UUID.randomUUID(), UUID.randomUUID(),
                toJson(new SecurityAlertPayload(email, event, details)), Instant.now());
        }
        record SecurityAlertPayload(String email, String event, String details) {}
    }

    record WelcomeEmail(String type, UUID eventId, UUID aggregateId, String payload, Instant createdAt) implements OutboxEvent {
        public WelcomeEmail(String email, String username) {
            this("welcome_email", UUID.randomUUID(), UUID.randomUUID(),
                toJson(new WelcomeEmailPayload(email, username)), Instant.now());
        }
        record WelcomeEmailPayload(String email, String username) {}
    }

    record StopCascadeLimitReached(String type, UUID eventId, UUID aggregateId, String payload, Instant createdAt) implements OutboxEvent {
        public StopCascadeLimitReached(Identifier.InstrumentId instrumentId, int count) {
            this("stop_cascade_limit_reached", UUID.randomUUID(), instrumentId.getValue(),
                toJson(new StopCascadePayload(instrumentId, count)), Instant.now());
        }
        record StopCascadePayload(Identifier.InstrumentId instrumentId, int count) {}
    }

    private static String toJson(Object payload) {
        try {
            return new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(payload);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize outbox event", e);
        }
    }
}
