package com.nextrade.contracts.common;

public record PageResponse<T>(java.util.List<T> items, int page, int size, long total) {
}
