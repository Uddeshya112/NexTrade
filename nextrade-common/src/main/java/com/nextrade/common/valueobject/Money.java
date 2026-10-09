package com.nextrade.common.valueobject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;
import java.util.Objects;

public record Money(BigDecimal amount, Currency currency) implements Comparable<Money> {
    public static final int SCALE = 4;
    public Money {
        Objects.requireNonNull(amount, "amount");
        Objects.requireNonNull(currency, "currency");
        amount = amount.setScale(SCALE, RoundingMode.HALF_EVEN);
    }
    public static Money zero(Currency currency){ return new Money(BigDecimal.ZERO,currency); }
    public static Money of(String amount,String currency){ return new Money(new BigDecimal(amount),Currency.getInstance(currency)); }
    public static Money of(BigDecimal amount,String currency){ return new Money(amount,Currency.getInstance(currency)); }
    private void same(Money o){ if(!currency.equals(o.currency)) throw new IllegalArgumentException("Currency mismatch"); }
    public Money add(Money o){same(o);return new Money(amount.add(o.amount),currency);}
    public Money subtract(Money o){same(o);return new Money(amount.subtract(o.amount),currency);}
    public Money multiply(BigDecimal factor){return new Money(amount.multiply(factor),currency);}
    public Money abs(){return new Money(amount.abs(),currency);}
    public Money negate(){return new Money(amount.negate(),currency);}
    public boolean isPositive(){return amount.signum()>0;} public boolean isNegative(){return amount.signum()<0;} public boolean isZero(){return amount.signum()==0;}
    @Override public int compareTo(Money o){same(o);return amount.compareTo(o.amount);} 
    @Override public String toString(){return amount.stripTrailingZeros().toPlainString()+" "+currency.getCurrencyCode();}
}
