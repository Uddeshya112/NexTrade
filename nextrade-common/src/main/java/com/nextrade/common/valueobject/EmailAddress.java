package com.nextrade.common.valueobject;
public record EmailAddress(String value) { public EmailAddress { if(value==null||value.isBlank()) throw new IllegalArgumentException("EmailAddress required"); } }
