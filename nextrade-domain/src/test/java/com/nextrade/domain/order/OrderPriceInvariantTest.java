package com.nextrade.domain.order;

import com.nextrade.common.enumtype.InstrumentStatus;
import com.nextrade.common.enumtype.OrderSide;
import com.nextrade.common.enumtype.TimeInForce;
import com.nextrade.common.enumtype.UserRole;
import com.nextrade.common.identifier.InstrumentId;
import com.nextrade.common.valueobject.Price;
import com.nextrade.common.valueobject.Quantity;
import com.nextrade.common.valueobject.Symbol;
import com.nextrade.domain.instrument.Instrument;
import com.nextrade.domain.user.User;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class OrderPriceInvariantTest {
    @Test
    void rejectsZeroLimitPrice() {
        assertThrows(RuntimeException.class, () -> Order.limit(user(), instrument(), OrderSide.BUY,
                Quantity.of("1"), Price.of("0", "0.01"), TimeInForce.GTC, "zero"));
    }

    @Test
    void rejectsPriceNotAlignedToInstrumentTickEvenWhenClientSuppliesSmallerTick() {
        assertThrows(RuntimeException.class, () -> Order.limit(user(), instrument(), OrderSide.BUY,
                Quantity.of("1"), Price.of("100.005", "0.001"), TimeInForce.GTC, "off-tick"));
    }

    private static User user() {
        return User.register("test-user", "test-user@example.test", "test-hash", UserRole.TRADER);
    }

    private static Instrument instrument() {
        return new Instrument(InstrumentId.generate(), new Symbol("NEX"), "NexTrade Test", "NEX", "INR", 1,
                Price.of("0.01", "0.01"), InstrumentStatus.ACTIVE);
    }
}
