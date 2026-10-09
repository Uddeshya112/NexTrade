package com.nextrade.common.valueobject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public record Price(BigDecimal value, BigDecimal tickSize) implements Comparable<Price> {
    public Price {
        Objects.requireNonNull(value,"value"); Objects.requireNonNull(tickSize,"tickSize");
        if(value.signum()<0) throw new IllegalArgumentException("Price cannot be negative");
        if(tickSize.signum()<=0) throw new IllegalArgumentException("Tick size must be positive");
        value=value.stripTrailingZeros(); tickSize=tickSize.stripTrailingZeros();
    }
    public static Price of(String value,String tick){return new Price(new BigDecimal(value),new BigDecimal(tick));}
    public static Price of(BigDecimal value,BigDecimal tick){return new Price(value,tick);}
    public Price roundToTick(){BigDecimal n=value.divide(tickSize,0,RoundingMode.HALF_EVEN).multiply(tickSize);return new Price(n,tickSize);}
    public Price plus(BigDecimal delta){return new Price(value.add(delta),tickSize);} public Price minus(BigDecimal delta){return new Price(value.subtract(delta),tickSize);}
    public boolean gte(Price o){return compareTo(o)>=0;} public boolean lte(Price o){return compareTo(o)<=0;} public boolean isZero(){return value.signum()==0;}
    @Override public int compareTo(Price o){return value.compareTo(o.value);} @Override public String toString(){return value.toPlainString();}
}
