package com.nextrade.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/portfolio")
@Tag(name = "Portfolio", description = "Portfolio management")
@SecurityRequirement(name = "bearerAuth")
public class PortfolioController {

    @GetMapping
    @Operation(summary = "Get portfolio summary")
    public ResponseEntity<java.util.Map<String, String>> getPortfolio() {
        // Do not turn an unimplemented accounting path into a misleading 500 or fabricated balance.
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(java.util.Map.of(
                "code", "PORTFOLIO_ACCOUNTING_UNAVAILABLE",
                "message", "Portfolio cash and position accounting is not available yet."));
    }
}
