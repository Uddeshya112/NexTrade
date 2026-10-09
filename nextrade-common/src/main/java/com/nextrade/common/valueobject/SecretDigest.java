package com.nextrade.common.valueobject;
public record SecretDigest(String value) { public SecretDigest { if(value==null||value.isBlank()) throw new IllegalArgumentException("SecretDigest required"); } }
