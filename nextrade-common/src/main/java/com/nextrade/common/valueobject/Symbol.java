package com.nextrade.common.valueobject;
public record Symbol(String value) { public Symbol { if(value==null||value.isBlank())throw new IllegalArgumentException("Symbol required"); value=value.trim().toUpperCase(java.util.Locale.ROOT); if(!value.matches("[A-Z0-9._-]{1,20}"))throw new IllegalArgumentException("Invalid symbol"); } }
