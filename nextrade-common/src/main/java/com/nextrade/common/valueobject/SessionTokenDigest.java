package com.nextrade.common.valueobject;
public record SessionTokenDigest(String value) { public SessionTokenDigest { if(value==null||value.isBlank()) throw new IllegalArgumentException("SessionTokenDigest required"); } }
