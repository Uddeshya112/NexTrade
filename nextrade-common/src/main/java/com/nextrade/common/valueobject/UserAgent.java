package com.nextrade.common.valueobject;
public record UserAgent(String value) { public UserAgent { if(value==null||value.isBlank()) throw new IllegalArgumentException("UserAgent required"); } }
