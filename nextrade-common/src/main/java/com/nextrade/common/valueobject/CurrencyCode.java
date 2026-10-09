package com.nextrade.common.valueobject;
public record CurrencyCode(String value) { public CurrencyCode { if(value==null||value.isBlank()) throw new IllegalArgumentException("CurrencyCode required"); } }
