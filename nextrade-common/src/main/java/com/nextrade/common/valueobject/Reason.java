package com.nextrade.common.valueobject;
public record Reason(String value) { public Reason { if(value==null||value.isBlank()) throw new IllegalArgumentException("Reason required"); } }
