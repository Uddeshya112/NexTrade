package com.nextrade.api.controller;

import com.nextrade.domain.instrument.Instrument;
import com.nextrade.domain.repository.InstrumentRepository;
import com.nextrade.common.valueobject.Price;
import com.nextrade.service.MarketDataService;
import com.nextrade.contracts.trading.MarketQuote;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/market")
@RequiredArgsConstructor
@Tag(name = "Market Data", description = "Market data endpoints")
public class MarketController {

    private final MarketDataService marketDataService;
    private final InstrumentRepository instruments;

    @GetMapping("/prices")
    @Operation(summary = "Get current prices for instruments")
    public ResponseEntity<Map<com.nextrade.common.identifier.InstrumentId, Price>> getPrices(@RequestParam List<UUID> instruments) {
        return ResponseEntity.ok(marketDataService.getCurrentPrices(instruments.stream()
                .map(com.nextrade.common.identifier.InstrumentId::of).collect(java.util.stream.Collectors.toSet())));
    }

    @GetMapping("/quotes")
    @Operation(summary = "Get timestamped simulated quotes; absent symbols have no quote")
    public ResponseEntity<Map<String, MarketQuote>> getQuotes(
            @RequestParam List<UUID> instruments) {
        Map<String, MarketQuote> quotes = new java.util.HashMap<>();
        for (var id : instruments.stream().map(com.nextrade.common.identifier.InstrumentId::of).collect(java.util.stream.Collectors.toSet())) {
            var quote = marketDataService.getQuote(id);
            if (quote != null) quotes.put(id.asString(), quote);
        }
        return ResponseEntity.ok(Map.copyOf(quotes));
    }

    @GetMapping("/instruments")
    @Operation(summary = "Get all active instruments")
    public ResponseEntity<List<InstrumentSummary>> getInstruments() {
        return ResponseEntity.ok(instruments.findActive().stream().map(InstrumentSummary::from).toList());
    }

    public record InstrumentSummary(String id, String symbol, String name, String exchange, String currency, boolean tradeable) {
        static InstrumentSummary from(Instrument instrument) {
            return new InstrumentSummary(instrument.id().asString(), instrument.symbol().value(), instrument.name(),
                    instrument.exchange(), instrument.currency(), instrument.tradeable());
        }
    }
}
