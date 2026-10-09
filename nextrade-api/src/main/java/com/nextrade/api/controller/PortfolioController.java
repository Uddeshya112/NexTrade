package com.nextrade.api.controller;

import com.nextrade.common.identifier.Identifier;
import com.nextrade.service.TradingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/portfolio")
@RequiredArgsConstructor
@Tag(name = "Portfolio", description = "Portfolio management")
@SecurityRequirement(name = "bearerAuth")
public class PortfolioController {

    private final TradingService tradingService;

    @GetMapping
    @Operation(summary = "Get portfolio summary")
    public ResponseEntity<com.nextrade.service.TradingService.PortfolioSummary> getPortfolio(@AuthenticationPrincipal Jwt jwt) {
        com.nextrade.common.identifier.UserId userId = com.nextrade.common.identifier.UserId.of(UUID.fromString(jwt.getSubject()));
        return ResponseEntity.ok(tradingService.getPortfolioSummary(userId));
    }
}
