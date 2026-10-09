package com.nextrade.domain.user;

import com.nextrade.common.identifier.UserId;
import com.nextrade.common.valueobject.Money;
import org.junit.jupiter.api.Test;

import java.util.Currency;

import static org.junit.jupiter.api.Assertions.*;

class WalletInvariantTest {
    private final Currency inr = Currency.getInstance("INR");

    @Test
    void negativeDepositAndWithdrawalAreRejected() {
        Wallet wallet = Wallet.create(UserId.generate(), inr);
        assertThrows(IllegalArgumentException.class, () -> wallet.deposit(Money.of("-10", "INR")));
        assertThrows(IllegalArgumentException.class, () -> wallet.withdraw(Money.of("-10", "INR")));
        assertEquals(0, wallet.available().amount().signum());
    }

    @Test
    void reservationsRejectNegativeAndZeroAmounts() {
        Wallet wallet = Wallet.create(UserId.generate(), inr);
        wallet.deposit(Money.of("100", "INR"));
        assertThrows(IllegalArgumentException.class, () -> wallet.reserve(Money.of("-5", "INR")));
        assertThrows(IllegalArgumentException.class, () -> wallet.reserve(Money.zero(inr)));
        assertEquals(Money.of("100", "INR"), wallet.available());
        assertEquals(Money.zero(inr), wallet.reserved());
    }

    @Test
    void reconstitutedWalletCannotStartWithNegativeBalances() {
        assertThrows(IllegalArgumentException.class, () -> new Wallet(
                com.nextrade.common.identifier.WalletId.generate(), UserId.generate(), inr,
                Money.of("-1", "INR"), Money.zero(inr)));
    }
}
