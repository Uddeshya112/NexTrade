package com.nextrade.persistence.adapter;

import com.nextrade.common.valueobject.Money;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.math.BigDecimal;

/** Stores a Money value as an amount and ISO-4217 currency code separated by a pipe. */
@Converter(autoApply = true)
public final class MoneyAttributeConverter implements AttributeConverter<Money, String> {
    private static final String SEPARATOR = "|";

    @Override
    public String convertToDatabaseColumn(Money money) {
        if (money == null) return null;
        return money.amount().toPlainString() + SEPARATOR + money.currency().getCurrencyCode();
    }

    @Override
    public Money convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;
        String[] parts = dbData.split("\\|", -1);
        if (parts.length != 2 || parts[0].isBlank() || parts[1].isBlank()) {
            throw new IllegalArgumentException("Invalid persisted Money value; expected amount|currency");
        }
        try {
            return Money.of(new BigDecimal(parts[0]), parts[1]);
        } catch (RuntimeException ex) {
            throw new IllegalArgumentException("Invalid persisted Money value", ex);
        }
    }
}
