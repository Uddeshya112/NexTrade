package com.nextrade.common.valueobject;
public record IpAddress(String value) { public IpAddress { if(value==null||value.isBlank()) throw new IllegalArgumentException("IpAddress required"); } }
