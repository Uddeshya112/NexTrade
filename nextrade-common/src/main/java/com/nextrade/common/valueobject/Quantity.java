package com.nextrade.common.valueobject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public record Quantity(BigDecimal value) implements Comparable<Quantity> {
    public static final int SCALE=4;
    public Quantity { Objects.requireNonNull(value,"value"); value=value.setScale(SCALE,RoundingMode.HALF_EVEN); if(value.signum()<0)throw new IllegalArgumentException("Quantity cannot be negative"); }
    public static Quantity zero(){return new Quantity(BigDecimal.ZERO);} public static Quantity of(String v){return new Quantity(new BigDecimal(v));}
    public Quantity add(Quantity o){return new Quantity(value.add(o.value));} public Quantity subtract(Quantity o){if(value.compareTo(o.value)<0)throw new IllegalArgumentException("Negative quantity");return new Quantity(value.subtract(o.value));}
    public Quantity min(Quantity o){return value.compareTo(o.value)<=0?this:o;} public Quantity max(Quantity o){return value.compareTo(o.value)>=0?this:o;}
    public Quantity multiply(BigDecimal m){return new Quantity(value.multiply(m));} public Quantity floor(BigDecimal step){return new Quantity(value.divide(step,0,RoundingMode.FLOOR).multiply(step));}
    public boolean isZero(){return value.signum()==0;} public boolean isPositive(){return value.signum()>0;}
    @Override public int compareTo(Quantity o){return value.compareTo(o.value);} @Override public String toString(){return value.stripTrailingZeros().toPlainString();}
}
