package com.nextrade.common.valueobject;
public record CorrelationId(String value) { public CorrelationId { if(value==null||value.isBlank()) throw new IllegalArgumentException("CorrelationId required"); } }
