package com.nextrade.domain.user;

import com.nextrade.common.exception.InsufficientFundsException;
import com.nextrade.common.identifier.UserId;
import com.nextrade.common.identifier.WalletId;
import com.nextrade.common.valueobject.Money;
import com.nextrade.domain.shared.AbstractAggregateRoot;

import java.util.Currency;
import java.util.Objects;

/** Wallet aggregate. Every cash movement must be positive and currency-consistent. */
public final class Wallet extends AbstractAggregateRoot<WalletId> {
    private final UserId userId;
    private final Currency currency;
    private Money available;
    private Money reserved;

    public Wallet(WalletId id, UserId userId, Currency currency, Money available, Money reserved) {
        super(id);
        this.userId = Objects.requireNonNull(userId, "userId");
        this.currency = Objects.requireNonNull(currency, "currency");
        this.available = Objects.requireNonNull(available, "available");
        this.reserved = Objects.requireNonNull(reserved, "reserved");
        sameCurrency(available);
        sameCurrency(reserved);
        if (available.isNegative() || reserved.isNegative()) {
            throw new IllegalArgumentException("Wallet balances cannot be negative");
        }
    }

    public static Wallet create(UserId user, Currency currency) {
        return new Wallet(WalletId.generate(), user, currency, Money.zero(currency), Money.zero(currency));
    }

    public void deposit(Money amount) {
        requirePositive(amount, "Deposit");
        available = available.add(amount);
        touch();
    }

    public void withdraw(Money amount) {
        requirePositive(amount, "Withdrawal");
        if (available.compareTo(amount) < 0) throw new InsufficientFundsException("Insufficient available funds");
        available = available.subtract(amount);
        touch();
    }

    public void reserve(Money amount) {
        requirePositive(amount, "Reservation");
        if (available.compareTo(amount) < 0) throw new InsufficientFundsException("Insufficient available funds");
        available = available.subtract(amount);
        reserved = reserved.add(amount);
        touch();
    }

    public void release(Money amount) {
        requirePositive(amount, "Release");
        if (reserved.compareTo(amount) < 0) throw new IllegalArgumentException("Release exceeds reservation");
        reserved = reserved.subtract(amount);
        available = available.add(amount);
        touch();
    }

    public void consumeReserved(Money amount) {
        requirePositive(amount, "Reserved-balance consumption");
        if (reserved.compareTo(amount) < 0) throw new IllegalArgumentException("Consume exceeds reservation");
        reserved = reserved.subtract(amount);
        touch();
    }

    private void requirePositive(Money amount, String operation) {
        sameCurrency(Objects.requireNonNull(amount, "amount"));
        if (!amount.isPositive()) throw new IllegalArgumentException(operation + " amount must be positive");
    }

    private void sameCurrency(Money amount) {
        if (!currency.equals(amount.currency())) throw new IllegalArgumentException("Currency mismatch");
    }

    public UserId userId() { return userId; }
    public Currency currency() { return currency; }
    public Money available() { return available; }
    public Money reserved() { return reserved; }
    public Money total() { return available.add(reserved); }
}
