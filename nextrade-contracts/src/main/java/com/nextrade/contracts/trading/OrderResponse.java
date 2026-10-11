package com.nextrade.contracts.trading;

import com.nextrade.common.enumtype.OrderStatus;
import com.nextrade.common.identifier.OrderId;

public record OrderResponse(String orderId, String status, String message, String revision) {
    public static OrderResponse pending(OrderId id, String clientId, OrderStatus status, java.util.List<?> trades) {
        return new OrderResponse(id.asString(), status.name(), "Order accepted", clientId);
    }
    public static OrderResponse placed(OrderId id, OrderStatus status, java.util.List<?> trades) {
        return new OrderResponse(id.asString(), status.name(), "Order placed", null);
    }
    public static OrderResponse rejected(OrderId id, String reason) {
        return new OrderResponse(id.asString(), "REJECTED", reason, null);
    }
    public static OrderResponse cancelled(OrderId id) {
        return new OrderResponse(id.asString(), "CANCELLED", "Order cancelled", null);
    }

    public record CancelResult(boolean success, String orderId, String message) {
        public static CancelResult cancelled(OrderId id) { return new CancelResult(true, id.asString(), "Cancelled"); }
        public static CancelResult notFound(OrderId id) { return new CancelResult(false, id.asString(), "Not found"); }
        public static CancelResult alreadyTerminal(OrderStatus status) { return new CancelResult(false, null, "Already " + status); }
        public static CancelResult failed(String message) { return new CancelResult(false, null, message); }
    }
}
