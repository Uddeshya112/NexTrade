package com.nextrade.persistence.adapter;

import com.nextrade.common.valueobject.Money;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class MoneyAttributeConverter implements AttributeConverter<Money, String> {
    @Override
    public String convertToDatabaseColumn(Money money) {
        return money.getAmount().toPlainString() + "|" + money.getCurrencyCode();
    }

    @Override
    public Money convertToEntityAttribute(String dbData) {
        String[] parts = dbData.split("\\|");
        return Money.of(new java.math.BigDecimal(parts[0]), java.util.Currency.getInstance(parts[1]));
    }
}
