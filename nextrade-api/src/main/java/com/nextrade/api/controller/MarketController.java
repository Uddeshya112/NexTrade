package com.nextrade.api.controller;

import com.nextrade.common.identifier.Identifier;
import com.nextrade.common.valueobject.Price;
import com.nextrade.service.MarketDataService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/market")
@RequiredArgsConstructor
@Tag(name = "Market Data", description = "Market data endpoints")
public class MarketController {

    private final MarketDataService marketDataService;

    @GetMapping("/prices")
    @Operation(summary = "Get current prices for instruments")
    public ResponseEntity<Map<Identifier.InstrumentId, Price>> getPrices(@RequestParam List<Identifier.InstrumentId> instruments) {
        return ResponseEntity.ok(marketDataService.getCurrentPrices(Set.copyOf(instruments)));
    }

    @GetMapping("/instruments")
    @Operation(summary = "Get all active instruments")
    public ResponseEntity<List<InstrumentSummary>> getInstruments() {
        // Implementation would fetch from InstrumentRepository
        return ResponseEntity.ok(List.of());
    }

    public record InstrumentSummary(Identifier.InstrumentId id, String symbol, String name, String exchange, String currency, boolean tradeable) {}
}
