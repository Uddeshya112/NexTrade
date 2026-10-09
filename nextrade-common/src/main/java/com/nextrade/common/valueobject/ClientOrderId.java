package com.nextrade.common.valueobject;
public record ClientOrderId(String value) { public ClientOrderId { if(value==null||value.isBlank()) throw new IllegalArgumentException("ClientOrderId required"); } }
