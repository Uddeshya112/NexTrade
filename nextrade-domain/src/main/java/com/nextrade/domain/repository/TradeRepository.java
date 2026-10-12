package com.nextrade.domain.repository;

import com.nextrade.common.identifier.*;
import com.nextrade.domain.order.Trade;
import java.util.*;

public interface TradeRepository {
    Optional<Trade> findById(TradeId id);
    Trade save(Trade trade);
    List<Trade> findByOrder(OrderId id);
    List<Trade> findByInstrument(InstrumentId id);
}
