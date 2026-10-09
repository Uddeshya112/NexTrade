package com.nextrade.common.identifier;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public sealed interface Identifier<T extends Identifier<T>> extends Serializable
        permits UserId, InstrumentId, OrderId, TradeId, StrategyId, WalletId, PortfolioId,
                SessionId, ReservationId, LedgerEntryId, IdempotencyId, EventId {
    UUID value();
    default String asString() { return value().toString(); }
}
