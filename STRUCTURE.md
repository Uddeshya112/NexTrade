# NexTrade Structured Project

This is the assembled NexTrade project tree. Concrete file blocks from the current pasted source replace matching paths; explicitly referenced files are sourced from the prior package and noted in the provenance manifest.

## File tree

├── docs
│   ├── historical
│   │   └── README-HISTORICAL.txt
│   ├── input
│   │   └── Pasted-text-20261008-214506.txt
│   ├── ARCHITECTURE.md
│   ├── CURRENT-PASTE-MANIFEST.md
│   ├── FILE-MANIFEST.txt
│   ├── FINAL-VERIFICATION-REPORT.md
│   ├── RECONCILIATION-OF-PREVIOUS-GENERATED-MATERIAL.md
│   ├── ROADMAP.md
│   ├── STRUCTURING-AND-PROVENANCE.md
│   └── VERIFICATION-STATUS.md
├── nextrade-api
│   ├── src
│   │   └── main
│   │       ├── java
│   │       │   └── com
│   │       │       └── nextrade
│   │       │           └── api
│   │       │               ├── config
│   │       │               │   ├── EngineConfiguration.java
│   │       │               │   ├── InMemoryAuthChallengeRepository.java
│   │       │               │   ├── InMemorySessionRepository.java
│   │       │               │   ├── InMemoryUserRepository.java
│   │       │               │   ├── JacksonConfig.java
│   │       │               │   └── ServiceConfiguration.java
│   │       │               ├── controller
│   │       │               │   ├── AuthController.java
│   │       │               │   ├── MarketController.java
│   │       │               │   ├── PortfolioController.java
│   │       │               │   ├── TradingController.java
│   │       │               │   └── UserControllerSupport.java
│   │       │               ├── exception
│   │       │               │   └── GlobalExceptionHandler.java
│   │       │               ├── security
│   │       │               │   ├── AccessTokenService.java
│   │       │               │   ├── JwtAuthenticationConverter.java
│   │       │               │   ├── JwtAuthenticationFilter.java
│   │       │               │   └── SecurityConfig.java
│   │       │               ├── websocket
│   │       │               │   ├── interceptor
│   │       │               │   │   └── JwtChannelInterceptor.java
│   │       │               │   ├── MarketDataWebSocketController.java
│   │       │               │   └── WebSocketConfig.java
│   │       │               └── NexTradeApiApplication.java
│   │       └── resources
│   │           ├── application.yml
│   │           └── logback-spring.xml
│   ├── Dockerfile
│   └── pom.xml
├── nextrade-common
│   ├── src
│   │   └── main
│   │       └── java
│   │           └── com
│   │               └── nextrade
│   │                   └── common
│   │                       ├── enumtype
│   │                       │   ├── CashAccountStatus.java
│   │                       │   ├── EventType.java
│   │                       │   ├── ExecutionReportType.java
│   │                       │   ├── InstrumentStatus.java
│   │                       │   ├── LedgerEntryType.java
│   │                       │   ├── MarketPhase.java
│   │                       │   ├── OrderSide.java
│   │                       │   ├── OrderStatus.java
│   │                       │   ├── OrderType.java
│   │                       │   ├── PositionSide.java
│   │                       │   ├── PriceTimePolicy.java
│   │                       │   ├── ReservationStatus.java
│   │                       │   ├── RiskSeverity.java
│   │                       │   ├── SessionStatus.java
│   │                       │   ├── TimeInForce.java
│   │                       │   ├── UserRole.java
│   │                       │   └── UserStatus.java
│   │                       ├── event
│   │                       │   ├── DomainEvent.java
│   │                       │   ├── FundsReleasedEvent.java
│   │                       │   ├── FundsReservedEvent.java
│   │                       │   ├── OrderAcceptedEvent.java
│   │                       │   ├── OrderCancelledEvent.java
│   │                       │   ├── OrderExpiredEvent.java
│   │                       │   ├── OrderRejectedEvent.java
│   │                       │   ├── OrderTriggeredEvent.java
│   │                       │   ├── SettlementPostedEvent.java
│   │                       │   ├── TradeExecutedEvent.java
│   │                       │   └── UserLockedEvent.java
│   │                       ├── exception
│   │                       │   ├── ConcurrentCommandException.java
│   │                       │   ├── IdempotencyConflictException.java
│   │                       │   ├── InstrumentNotTradeableException.java
│   │                       │   ├── InsufficientFundsException.java
│   │                       │   ├── InvalidCredentialsException.java
│   │                       │   ├── InvalidOrderException.java
│   │                       │   ├── NexTradeException.java
│   │                       │   ├── OrderNotFoundException.java
│   │                       │   ├── RateLimitExceededException.java
│   │                       │   ├── RiskLimitExceededException.java
│   │                       │   ├── SettlementException.java
│   │                       │   ├── TokenException.java
│   │                       │   └── UnauthorizedException.java
│   │                       ├── identifier
│   │                       │   ├── EventId.java
│   │                       │   ├── IdempotencyId.java
│   │                       │   ├── Identifier.java
│   │                       │   ├── InstrumentId.java
│   │                       │   ├── LedgerEntryId.java
│   │                       │   ├── OrderId.java
│   │                       │   ├── PortfolioId.java
│   │                       │   ├── ReservationId.java
│   │                       │   ├── SessionId.java
│   │                       │   ├── StrategyId.java
│   │                       │   ├── TradeId.java
│   │                       │   ├── UserId.java
│   │                       │   └── WalletId.java
│   │                       ├── util
│   │                       │   ├── Backoff.java
│   │                       │   ├── ClockProvider.java
│   │                       │   ├── DecimalMath.java
│   │                       │   ├── Hashing.java
│   │                       │   ├── IdempotencyKeys.java
│   │                       │   ├── JsonSafe.java
│   │                       │   ├── Pagination.java
│   │                       │   ├── RandomIds.java
│   │                       │   ├── SafeStrings.java
│   │                       │   ├── TimeBuckets.java
│   │                       │   └── Validation.java
│   │                       └── valueobject
│   │                           ├── ClientOrderId.java
│   │                           ├── CorrelationId.java
│   │                           ├── CurrencyCode.java
│   │                           ├── EmailAddress.java
│   │                           ├── IpAddress.java
│   │                           ├── Money.java
│   │                           ├── Price.java
│   │                           ├── Quantity.java
│   │                           ├── Reason.java
│   │                           ├── SecretDigest.java
│   │                           ├── SessionTokenDigest.java
│   │                           ├── Symbol.java
│   │                           └── UserAgent.java
│   └── pom.xml
├── nextrade-contracts
│   ├── src
│   │   └── main
│   │       └── java
│   │           └── com
│   │               └── nextrade
│   │                   └── contracts
│   │                       ├── auth
│   │                       │   ├── LoginRequest.java
│   │                       │   ├── LogoutRequest.java
│   │                       │   ├── RefreshRequest.java
│   │                       │   ├── RegisterRequest.java
│   │                       │   ├── ResetPasswordRequest.java
│   │                       │   └── VerifyEmailRequest.java
│   │                       ├── common
│   │                       │   ├── AckResponse.java
│   │                       │   ├── ApiError.java
│   │                       │   └── PageResponse.java
│   │                       ├── strategy
│   │                       │   ├── StrategyDefinition.java
│   │                       │   └── StrategySignal.java
│   │                       └── trading
│   │                           ├── CancelOrderRequest.java
│   │                           ├── HealthResponse.java
│   │                           ├── MarketQuote.java
│   │                           ├── ModifyOrderRequest.java
│   │                           ├── OrderBookLevel.java
│   │                           ├── OrderBookResponse.java
│   │                           ├── OrderRequest.java
│   │                           ├── OrderResponse.java
│   │                           ├── PortfolioHolding.java
│   │                           ├── PortfolioResponse.java
│   │                           └── TradeResponse.java
│   └── pom.xml
├── nextrade-domain
│   ├── src
│   │   └── main
│   │       └── java
│   │           └── com
│   │               └── nextrade
│   │                   └── domain
│   │                       ├── instrument
│   │                       │   └── Instrument.java
│   │                       ├── ledger
│   │                       │   ├── Deposit.java
│   │                       │   ├── Fee.java
│   │                       │   ├── LedgerEntry.java
│   │                       │   ├── Release.java
│   │                       │   ├── Reservation.java
│   │                       │   ├── Tax.java
│   │                       │   ├── TradeCredit.java
│   │                       │   ├── TradeDebit.java
│   │                       │   └── Withdrawal.java
│   │                       ├── market
│   │                       │   └── MarketState.java
│   │                       ├── order
│   │                       │   ├── Order.java
│   │                       │   ├── OrderInstruction.java
│   │                       │   └── Trade.java
│   │                       ├── portfolio
│   │                       │   ├── Holding.java
│   │                       │   └── Portfolio.java
│   │                       ├── repository
│   │                       │   ├── InstrumentRepository.java
│   │                       │   ├── OrderRepository.java
│   │                       │   ├── PortfolioRepository.java
│   │                       │   ├── TradeRepository.java
│   │                       │   ├── UserRepository.java
│   │                       │   └── WalletRepository.java
│   │                       ├── risk
│   │                       │   ├── CashReservation.java
│   │                       │   └── RiskLimits.java
│   │                       ├── shared
│   │                       │   ├── AbstractAggregateRoot.java
│   │                       │   ├── AggregateRoot.java
│   │                       │   └── Entity.java
│   │                       └── user
│   │                           ├── Email.java
│   │                           ├── User.java
│   │                           ├── Username.java
│   │                           └── Wallet.java
│   └── pom.xml
├── nextrade-engine
│   ├── src
│   │   ├── main
│   │   │   └── java
│   │   │       └── com
│   │   │           └── nextrade
│   │   │               └── engine
│   │   │                   ├── market
│   │   │                   │   ├── MarketDataBuffer.java
│   │   │                   │   └── MarketTick.java
│   │   │                   ├── matching
│   │   │                   │   ├── FokLiquidityCheck.java
│   │   │                   │   ├── MatchingEngine.java
│   │   │                   │   ├── MatchingResult.java
│   │   │                   │   ├── OrderBook.java
│   │   │                   │   ├── OrderBookSnapshot.java
│   │   │                   │   ├── OrderPriority.java
│   │   │                   │   ├── PriceLevel.java
│   │   │                   │   ├── SelfTradePrevention.java
│   │   │                   │   ├── StopTriggerPolicy.java
│   │   │                   │   └── TradeExecution.java
│   │   │                   ├── policy
│   │   │                   │   ├── AuctionParticipationRule.java
│   │   │                   │   ├── AuditCompletenessRule.java
│   │   │                   │   ├── BookCrossRule.java
│   │   │                   │   ├── BookLevelRule.java
│   │   │                   │   ├── BookQuantityRule.java
│   │   │                   │   ├── BorrowAvailabilityRule.java
│   │   │                   │   ├── CancelFrequencyRule.java
│   │   │                   │   ├── CancelRejectRule.java
│   │   │                   │   ├── CashAvailabilityRule.java
│   │   │                   │   ├── CashBufferRule.java
│   │   │                   │   ├── CircuitBreakerRule.java
│   │   │                   │   ├── ConcentrationRule.java
│   │   │                   │   ├── CorrelationRule.java
│   │   │                   │   ├── DailyLossPolicy.java
│   │   │                   │   ├── DailyTurnoverRule.java
│   │   │                   │   ├── DataRetentionRule.java
│   │   │                   │   ├── DayExpiryRule.java
│   │   │                   │   ├── DeterminismRule.java
│   │   │                   │   ├── DrawdownRule.java
│   │   │                   │   ├── DuplicateClientOrderRule.java
│   │   │                   │   ├── EngineBackpressureRule.java
│   │   │                   │   ├── EngineLaneRule.java
│   │   │                   │   ├── EngineShutdownRule.java
│   │   │                   │   ├── ErrorMappingRule.java
│   │   │                   │   ├── EventOrderRule.java
│   │   │                   │   ├── ExecutionPriceRule.java
│   │   │                   │   ├── ExecutionSequenceRule.java
│   │   │                   │   ├── ExecutionTimestampRule.java
│   │   │                   │   ├── FeeCapRule.java
│   │   │                   │   ├── FeeFloorRule.java
│   │   │                   │   ├── FokLiquidityRule.java
│   │   │                   │   ├── FundingRule.java
│   │   │                   │   ├── GracefulDegradationRule.java
│   │   │                   │   ├── GrossExposureRule.java
│   │   │                   │   ├── GtcPersistenceRule.java
│   │   │                   │   ├── HeaderRule.java
│   │   │                   │   ├── HealthRule.java
│   │   │                   │   ├── HolidayRule.java
│   │   │                   │   ├── IdempotencyRule.java
│   │   │                   │   ├── InstrumentStatusRule.java
│   │   │                   │   ├── IocRemainderRule.java
│   │   │                   │   ├── LatencyBudgetRule.java
│   │   │                   │   ├── LedgerBalanceRule.java
│   │   │                   │   ├── LedgerCurrencyRule.java
│   │   │                   │   ├── LeverageRule.java
│   │   │                   │   ├── LiquidityRule.java
│   │   │                   │   ├── LockoutRule.java
│   │   │                   │   ├── LoginAttemptRule.java
│   │   │                   │   ├── LotSizeRule.java
│   │   │                   │   ├── MarginUtilizationRule.java
│   │   │                   │   ├── MarketPhaseRule.java
│   │   │                   │   ├── MarketRemainderRule.java
│   │   │                   │   ├── MaxPriceDistanceRule.java
│   │   │                   │   ├── MaxQuantityStepRule.java
│   │   │                   │   ├── MaximumNotionalRule.java
│   │   │                   │   ├── MetricsRule.java
│   │   │                   │   ├── MinimumNotionalRule.java
│   │   │                   │   ├── ModifyFrequencyRule.java
│   │   │                   │   ├── MonotonicClockRule.java
│   │   │                   │   ├── NetExposureRule.java
│   │   │                   │   ├── OpenOrderRule.java
│   │   │                   │   ├── OrderAgeRule.java
│   │   │                   │   ├── OrderNotionalRule.java
│   │   │                   │   ├── OrderQuantityRule.java
│   │   │                   │   ├── OriginRule.java
│   │   │                   │   ├── OutboxClaimRule.java
│   │   │                   │   ├── OutboxIdempotencyRule.java
│   │   │                   │   ├── OutboxRetryRule.java
│   │   │                   │   ├── OutboxStatusRule.java
│   │   │                   │   ├── PartitionRule.java
│   │   │                   │   ├── PasswordStrengthRule.java
│   │   │                   │   ├── PnlSanityRule.java
│   │   │                   │   ├── PortfolioValueRule.java
│   │   │                   │   ├── PositionAvailabilityRule.java
│   │   │                   │   ├── PriceBandRule.java
│   │   │                   │   ├── PriorityRule.java
│   │   │                   │   ├── QueuedOrderRule.java
│   │   │                   │   ├── RateLimitRule.java
│   │   │                   │   ├── ReadinessRule.java
│   │   │                   │   ├── RecoveryRule.java
│   │   │                   │   ├── ReferencePricePolicy.java
│   │   │                   │   ├── RefreshDigestRule.java
│   │   │                   │   ├── ReplayRecoveryRule.java
│   │   │                   │   ├── ReplayRule.java
│   │   │                   │   ├── ReserveReleaseRule.java
│   │   │                   │   ├── ResetTokenRule.java
│   │   │                   │   ├── SchemaVersionRule.java
│   │   │                   │   ├── SectorExposureRule.java
│   │   │                   │   ├── SelfTradeRule.java
│   │   │                   │   ├── SequenceOverflowRule.java
│   │   │                   │   ├── SequenceRule.java
│   │   │                   │   ├── SessionRotationRule.java
│   │   │                   │   ├── SessionRule.java
│   │   │                   │   ├── SettlementWindowRule.java
│   │   │                   │   ├── ShortSaleRule.java
│   │   │                   │   ├── SnapshotDepthRule.java
│   │   │                   │   ├── SnapshotRecoveryRule.java
│   │   │                   │   ├── SnapshotSequenceRule.java
│   │   │                   │   ├── SpreadGuardRule.java
│   │   │                   │   ├── StaleQuoteRule.java
│   │   │                   │   ├── StopCascadeRule.java
│   │   │                   │   ├── StopDepthRule.java
│   │   │                   │   ├── StopTriggerRule.java
│   │   │                   │   ├── TaxRule.java
│   │   │                   │   ├── TickSizeRule.java
│   │   │                   │   ├── TokenExpiryRule.java
│   │   │                   │   ├── TokenKidRule.java
│   │   │                   │   ├── TokenTypeRule.java
│   │   │                   │   ├── TradePriceRule.java
│   │   │                   │   ├── TradeQuantityRule.java
│   │   │                   │   ├── TradeSizeRule.java
│   │   │                   │   ├── VerificationAttemptRule.java
│   │   │                   │   ├── VolatilityGuardRule.java
│   │   │                   │   ├── WebhookSignatureRule.java
│   │   │                   │   └── WeekendRule.java
│   │   │                   ├── risk
│   │   │                   │   ├── DailyLossRule.java
│   │   │                   │   ├── MaxOpenOrdersRule.java
│   │   │                   │   ├── MaxOrderQuantityRule.java
│   │   │                   │   ├── ReferencePriceRule.java
│   │   │                   │   ├── RiskContext.java
│   │   │                   │   ├── RiskDecision.java
│   │   │                   │   ├── RiskEngine.java
│   │   │                   │   └── RiskRule.java
│   │   │                   ├── settlement
│   │   │                   │   ├── FeeSchedule.java
│   │   │                   │   ├── SettlementEngine.java
│   │   │                   │   └── SettlementInstruction.java
│   │   │                   ├── strategy
│   │   │                   │   ├── BreakoutStrategy.java
│   │   │                   │   ├── GridStrategy.java
│   │   │                   │   ├── MacdStrategy.java
│   │   │                   │   ├── MarketMakerStrategy.java
│   │   │                   │   ├── MeanReversionStrategy.java
│   │   │                   │   ├── MomentumStrategy.java
│   │   │                   │   ├── MovingAverageCrossStrategy.java
│   │   │                   │   ├── PairTradingStrategy.java
│   │   │                   │   ├── RsiStrategy.java
│   │   │                   │   ├── Strategy.java
│   │   │                   │   ├── StrategyEngine.java
│   │   │                   │   ├── TrendFollowingStrategy.java
│   │   │                   │   ├── VolatilityBreakoutStrategy.java
│   │   │                   │   └── VwapStrategy.java
│   │   │                   └── support
│   │   │                       ├── AuctionCalculator.java
│   │   │                       ├── CircuitBreaker.java
│   │   │                       ├── ExecutionTape.java
│   │   │                       ├── LatencyTracker.java
│   │   │                       ├── LotSizeValidator.java
│   │   │                       ├── MarketSession.java
│   │   │                       ├── MatchingEngineStats.java
│   │   │                       ├── NotionalCalculator.java
│   │   │                       ├── OrderBookInvariant.java
│   │   │                       ├── OrderExpiryScanner.java
│   │   │                       ├── PriceBand.java
│   │   │                       ├── ReferencePriceModel.java
│   │   │                       ├── SequenceClock.java
│   │   │                       ├── SlippageModel.java
│   │   │                       ├── TickValidator.java
│   │   │                       ├── TradeAggregator.java
│   │   │                       └── VolumeProfile.java
│   │   └── test
│   │       └── java
│   │           └── com
│   │               └── nextrade
│   │                   └── engine
│   │                       └── matching
│   │                           └── OrderBookPropertyTest.java
│   └── pom.xml
├── nextrade-fx-client
│   ├── src
│   │   └── main
│   │       ├── java
│   │       │   └── com
│   │       │       └── nextrade
│   │       │           └── client
│   │       │               ├── bootstrap
│   │       │               │   └── NexTradeApplication.java
│   │       │               ├── core
│   │       │               │   ├── ApiSession.java
│   │       │               │   ├── ClientClock.java
│   │       │               │   ├── ClientValidation.java
│   │       │               │   ├── DIContainer.java
│   │       │               │   └── NavigationState.java
│   │       │               ├── security
│   │       │               │   ├── AuthContext.java
│   │       │               │   ├── InMemoryTokenStorage.java
│   │       │               │   ├── RateLimiter.java
│   │       │               │   └── TokenStorage.java
│   │       │               ├── service
│   │       │               │   ├── ApiClient.java
│   │       │               │   ├── HttpClientService.java
│   │       │               │   ├── MarketStream.java
│   │       │               │   ├── OrderEntryService.java
│   │       │               │   ├── PortfolioQueryService.java
│   │       │               │   └── StompWebSocketClient.java
│   │       │               ├── view
│   │       │               │   └── LoginView.java
│   │       │               └── viewmodel
│   │       │                   ├── auth
│   │       │                   │   ├── ForgotPasswordViewModel.java
│   │       │                   │   └── LoginViewModel.java
│   │       │                   ├── LoginViewModel.java
│   │       │                   ├── MarketViewModel.java
│   │       │                   ├── OrderEntryViewModel.java
│   │       │                   ├── PortfolioViewModel.java
│   │       │                   └── SettingsViewModel.java
│   │       └── resources
│   │           ├── css
│   │           │   └── nextrade-dark.css
│   │           └── fxml
│   │               └── main.fxml
│   └── pom.xml
├── nextrade-integration-tests
│   ├── src
│   │   └── test
│   │       └── java
│   │           └── com
│   │               └── nextrade
│   │                   ├── integration
│   │                   │   ├── ArchitectureRulesTest.java
│   │                   │   ├── MatchingEngineConcurrencyTest.java
│   │                   │   └── TradingFlowIntegrationTest.java
│   │                   └── tests
│   │                       ├── ArchitectureNoEngineServiceTest.java
│   │                       ├── ArchitectureRulesTest.java
│   │                       ├── ArchitectureSingleJwtTest.java
│   │                       ├── ArchitectureStopIdentityTest.java
│   │                       ├── ArchiveNoPlaceholderTest.java
│   │                       ├── AuctionClearingPriceTest.java
│   │                       ├── AuctionParticipationTest.java
│   │                       ├── AuditFieldsTest.java
│   │                       ├── BackoffBoundsTest.java
│   │                       ├── BorrowGuardTest.java
│   │                       ├── BuyLimitMatchesSellLimitTest.java
│   │                       ├── BuyMarketConsumesLevelsTest.java
│   │                       ├── BuyStopTriggersOnRiseTest.java
│   │                       ├── CancelMissingOrderTest.java
│   │                       ├── CancelRestingOrderTest.java
│   │                       ├── CancelStopOrderTest.java
│   │                       ├── CashBufferCheckTest.java
│   │                       ├── CircuitBreakerRecoveryTest.java
│   │                       ├── CircuitBreakerTripTest.java
│   │                       ├── CircuitGuardTest.java
│   │                       ├── ClientMethodPreservedOnRetryTest.java
│   │                       ├── ClientOrderIdUniqueTest.java
│   │                       ├── ConcentrationCheckTest.java
│   │                       ├── ConcurrentDifferentInstrumentsProgressTest.java
│   │                       ├── ConcurrentSameInstrumentIsSerializedTest.java
│   │                       ├── ConfigurationDefaultsTest.java
│   │                       ├── CorrelationPropagationTest.java
│   │                       ├── CorsOriginParsingTest.java
│   │                       ├── DataRetentionTest.java
│   │                       ├── DayOrderExpiryTest.java
│   │                       ├── DrawdownCheckTest.java
│   │                       ├── EmptyBookSafeTest.java
│   │                       ├── EngineCloseIsIdempotentTest.java
│   │                       ├── EngineScenario01Test.java
│   │                       ├── EngineScenario02Test.java
│   │                       ├── EngineScenario03Test.java
│   │                       ├── EngineScenario04Test.java
│   │                       ├── EngineScenario05Test.java
│   │                       ├── EngineScenario06Test.java
│   │                       ├── EngineScenario07Test.java
│   │                       ├── EngineScenario08Test.java
│   │                       ├── EngineScenario09Test.java
│   │                       ├── EngineScenario10Test.java
│   │                       ├── EngineScenario11Test.java
│   │                       ├── EngineScenario12Test.java
│   │                       ├── EngineScenario13Test.java
│   │                       ├── EngineScenario14Test.java
│   │                       ├── EngineScenario15Test.java
│   │                       ├── EngineScenario16Test.java
│   │                       ├── EngineScenario17Test.java
│   │                       ├── EngineScenario18Test.java
│   │                       ├── EngineScenario19Test.java
│   │                       ├── EngineScenario20Test.java
│   │                       ├── ErrorMappingTest.java
│   │                       ├── EventDrainOnceTest.java
│   │                       ├── EventOrderTest.java
│   │                       ├── ExecutionOrderingTest.java
│   │                       ├── FeeCapTest.java
│   │                       ├── FeeFloorTest.java
│   │                       ├── Fixture.java
│   │                       ├── FokAcceptsEnoughLiquidityTest.java
│   │                       ├── FokRejectsInsufficientLiquidityTest.java
│   │                       ├── FullFillRemovesRestingTest.java
│   │                       ├── GracefulShutdownTest.java
│   │                       ├── GtcOrderRestTest.java
│   │                       ├── HashConstantTimeTest.java
│   │                       ├── HeaderPolicyTest.java
│   │                       ├── HealthEndpointTest.java
│   │                       ├── IdempotencyKeyValidationTest.java
│   │                       ├── IdentifierRoundTripTest.java
│   │                       ├── IdentifierTest.java
│   │                       ├── IocCancelsRemainderTest.java
│   │                       ├── JwtExpiredRejectedTest.java
│   │                       ├── JwtHeaderKidTest.java
│   │                       ├── JwtUnknownKidRejectedTest.java
│   │                       ├── JwtWrongTokenTypeTest.java
│   │                       ├── LatencyBudgetTest.java
│   │                       ├── LedgerFeeTest.java
│   │                       ├── LedgerTradeCreditTest.java
│   │                       ├── LedgerTradeDebitTest.java
│   │                       ├── LeverageCheckTest.java
│   │                       ├── LiquidityGuardTest.java
│   │                       ├── LoginLockoutAfterThresholdTest.java
│   │                       ├── MarginUtilizationTest.java
│   │                       ├── MarketCancelsRemainderTest.java
│   │                       ├── MarketDataRingBufferTest.java
│   │                       ├── MarketQuoteCrossedTest.java
│   │                       ├── MarketQuoteMidpointTest.java
│   │                       ├── MetricsCountersTest.java
│   │                       ├── ModifyNoBelowFilledTest.java
│   │                       ├── ModifyNoQuantityIncreaseTest.java
│   │                       ├── ModifyRetainsIdTest.java
│   │                       ├── MoneyCurrencySafetyTest.java
│   │                       ├── MoneyTest.java
│   │                       ├── OrderAveragePriceTest.java
│   │                       ├── OrderBookAskDepthTest.java
│   │                       ├── OrderBookBidDepthTest.java
│   │                       ├── OrderBookCountTest.java
│   │                       ├── OrderBookLastTradeTest.java
│   │                       ├── OrderBookSpreadTest.java
│   │                       ├── OrderExpirationTest.java
│   │                       ├── OrderLifecycleTest.java
│   │                       ├── OrderRequestValidationTest.java
│   │                       ├── OrderRevisionTest.java
│   │                       ├── OriginPolicyTest.java
│   │                       ├── OutboxClaimOnceTest.java
│   │                       ├── OutboxOrderingTest.java
│   │                       ├── OutboxRetryBackoffTest.java
│   │                       ├── PaginationBoundsTest.java
│   │                       ├── PartialFillCarriesForwardTest.java
│   │                       ├── PartitionHandlesMinValueTest.java
│   │                       ├── PartitionIsStableTest.java
│   │                       ├── PasswordResetAtomicTest.java
│   │                       ├── PasswordStrengthTest.java
│   │                       ├── PortfolioBuyTest.java
│   │                       ├── PortfolioReservationTest.java
│   │                       ├── PortfolioSellTest.java
│   │                       ├── PortfolioTest.java
│   │                       ├── PortfolioValuationTest.java
│   │                       ├── PositionAvailableQuantityTest.java
│   │                       ├── PositionAveragePriceTest.java
│   │                       ├── PositionRealizedPnlTest.java
│   │                       ├── PriceNegativeRejectedTest.java
│   │                       ├── PriceTest.java
│   │                       ├── PriceTimePriorityTest.java
│   │                       ├── PriceVolumeProfileTest.java
│   │                       ├── QuantityNegativeRejectedTest.java
│   │                       ├── QuantityTest.java
│   │                       ├── QueueFairnessTest.java
│   │                       ├── RateLimiterWindowTest.java
│   │                       ├── ReadinessDependencyTest.java
│   │                       ├── RecoveryReplayTest.java
│   │                       ├── ReferencePriceCalculationTest.java
│   │                       ├── RefreshRotationAtomicTest.java
│   │                       ├── RiskAllowedWhenAllPassTest.java
│   │                       ├── RiskDailyLossTest.java
│   │                       ├── RiskEngineTest.java
│   │                       ├── RiskMaxNotionalTest.java
│   │                       ├── RiskMaxQuantityTest.java
│   │                       ├── RiskOpenOrdersTest.java
│   │                       ├── RiskReferencePriceTest.java
│   │                       ├── SafeMaskingTest.java
│   │                       ├── SchemaMigrationOrderingTest.java
│   │                       ├── SelfTradeAvoidanceTest.java
│   │                       ├── SellLimitMatchesBuyLimitTest.java
│   │                       ├── SellMarketConsumesLevelsTest.java
│   │                       ├── SellStopTriggersOnFallTest.java
│   │                       ├── SequenceClockTest.java
│   │                       ├── SequenceIsMonotonicTest.java
│   │                       ├── SettlementFeeTest.java
│   │                       ├── SettlementGrossTest.java
│   │                       ├── SettlementNetTest.java
│   │                       ├── ShortSaleGuardTest.java
│   │                       ├── SlippageCalculationTest.java
│   │                       ├── SnapshotIsConsistentTest.java
│   │                       ├── SnapshotRestoreTest.java
│   │                       ├── SpreadGuardTest.java
│   │                       ├── StaleQuoteGuardTest.java
│   │                       ├── StopCascadeTerminatesTest.java
│   │                       ├── StopKeepsIdentityTest.java
│   │                       ├── StopTriggerPolicyTest.java
│   │                       ├── StopUsesLastTradeOnlyTest.java
│   │                       ├── StrategyRegistrationTest.java
│   │                       ├── StrategyRemovalTest.java
│   │                       ├── SymbolNormalizationTest.java
│   │                       ├── TaxCalculationTest.java
│   │                       ├── TerminalCancelSafeTest.java
│   │                       ├── TradeDirectionTest.java
│   │                       ├── TradeNotionalTest.java
│   │                       ├── UserLockoutTest.java
│   │                       ├── UserPasswordChangeTest.java
│   │                       ├── UserSuccessfulLoginTest.java
│   │                       ├── UserSuspensionTest.java
│   │                       ├── UserVerificationTest.java
│   │                       ├── VerificationCorrectCodeConsumesTest.java
│   │                       ├── VerificationWrongCodeCountsTest.java
│   │                       ├── VolatilityGuardTest.java
│   │                       ├── VolumeAggregationTest.java
│   │                       ├── WalletConsumeTest.java
│   │                       ├── WalletCurrencySafetyTest.java
│   │                       ├── WalletReleaseTest.java
│   │                       ├── WalletReserveTest.java
│   │                       ├── WalletTest.java
│   │                       ├── WebSocketInvalidAuthRejectedTest.java
│   │                       ├── WebSocketMissingAuthRejectedTest.java
│   │                       ├── WebSocketSendAuthenticatedTest.java
│   │                       └── WebhookSignatureTest.java
│   └── pom.xml
├── nextrade-persistence
│   ├── src
│   │   └── main
│   │       ├── java
│   │       │   └── com
│   │       │       └── nextrade
│   │       │           └── persistence
│   │       │               ├── adapter
│   │       │               │   ├── InMemoryRepositoryAdapter.java
│   │       │               │   ├── MoneyAttributeConverter.java
│   │       │               │   ├── OrderRepositoryAdapter.java
│   │       │               │   ├── OutboxRepositoryAdapter.java
│   │       │               │   └── UserRepositoryAdapter.java
│   │       │               ├── entity
│   │       │               │   ├── EmailVerificationEntity.java
│   │       │               │   ├── EmailVerificationTokenJpaEntity.java
│   │       │               │   ├── HoldingJpaEntity.java
│   │       │               │   ├── IdempotencyEntity.java
│   │       │               │   ├── InstrumentJpaEntity.java
│   │       │               │   ├── OrderEntity.java
│   │       │               │   ├── OrderJpaEntity.java
│   │       │               │   ├── OutboxEntity.java
│   │       │               │   ├── OutboxEventEntity.java
│   │       │               │   ├── PasswordResetTokenEntity.java
│   │       │               │   ├── PasswordResetTokenJpaEntity.java
│   │       │               │   ├── PortfolioJpaEntity.java
│   │       │               │   ├── TradeEntity.java
│   │       │               │   ├── TradeJpaEntity.java
│   │       │               │   ├── UserEntity.java
│   │       │               │   ├── UserJpaEntity.java
│   │       │               │   ├── UserSessionEntity.java
│   │       │               │   ├── UserSessionJpaEntity.java
│   │       │               │   ├── WalletEntity.java
│   │       │               │   └── WalletJpaEntity.java
│   │       │               └── repository
│   │       │                   ├── EmailVerificationJpaRepository.java
│   │       │                   ├── EmailVerificationTokenJpaRepository.java
│   │       │                   ├── IdempotencyJpaRepository.java
│   │       │                   ├── OrderJpaRepository.java
│   │       │                   ├── OutboxEventRepository.java
│   │       │                   ├── OutboxJpaRepository.java
│   │       │                   ├── PasswordResetTokenJpaRepository.java
│   │       │                   ├── TradeJpaRepository.java
│   │       │                   ├── UserJpaRepository.java
│   │       │                   ├── UserSessionJpaRepository.java
│   │       │                   └── WalletJpaRepository.java
│   │       └── resources
│   │           └── db
│   │               └── migration
│   │                   ├── V1__init_schema.sql
│   │                   └── V2__atomic_auth.sql
│   └── pom.xml
├── nextrade-service
│   ├── src
│   │   └── main
│   │       └── java
│   │           └── com
│   │               └── nextrade
│   │                   └── service
│   │                       ├── email
│   │                       │   ├── EmailService.java
│   │                       │   └── OutboxEventPublisher.java
│   │                       ├── policy
│   │                       │   ├── ApiVersionPolicy.java
│   │                       │   ├── AuditPolicy.java
│   │                       │   ├── AuthenticationPolicy.java
│   │                       │   ├── AuthorizationPolicy.java
│   │                       │   ├── CachingPolicy.java
│   │                       │   ├── CircuitPolicy.java
│   │                       │   ├── ConcurrencyPolicy.java
│   │                       │   ├── ConfigurationPolicy.java
│   │                       │   ├── ConsistencyPolicy.java
│   │                       │   ├── CorrelationPolicy.java
│   │                       │   ├── CorsPolicy.java
│   │                       │   ├── ErrorPolicy.java
│   │                       │   ├── FeePolicy.java
│   │                       │   ├── HealthPolicy.java
│   │                       │   ├── IdempotencyPolicy.java
│   │                       │   ├── LedgerPolicy.java
│   │                       │   ├── LoginLockoutPolicy.java
│   │                       │   ├── MarketDataPolicy.java
│   │                       │   ├── NotificationPolicy.java
│   │                       │   ├── OrderCommandPolicy.java
│   │                       │   ├── OrderQueryPolicy.java
│   │                       │   ├── OutboxPolicy.java
│   │                       │   ├── PaginationPolicy.java
│   │                       │   ├── PasswordChangePolicy.java
│   │                       │   ├── PasswordResetPolicy.java
│   │                       │   ├── PortfolioPolicy.java
│   │                       │   ├── RateLimitPolicy.java
│   │                       │   ├── ReadinessPolicy.java
│   │                       │   ├── RecoveryPolicy.java
│   │                       │   ├── RefreshRotationPolicy.java
│   │                       │   ├── ReplayPolicy.java
│   │                       │   ├── RetentionPolicy.java
│   │                       │   ├── RetryPolicy.java
│   │                       │   ├── RiskPolicy.java
│   │                       │   ├── SecretPolicy.java
│   │                       │   ├── SerializationPolicy.java
│   │                       │   ├── SessionPolicy.java
│   │                       │   ├── SettlementPolicy.java
│   │                       │   ├── SnapshotPolicy.java
│   │                       │   ├── TimeoutPolicy.java
│   │                       │   ├── TransactionPolicy.java
│   │                       │   ├── ValidationPolicy.java
│   │                       │   ├── VerificationPolicy.java
│   │                       │   └── WebSocketPolicy.java
│   │                       ├── ports
│   │                       │   ├── AccessTokenIssuer.java
│   │                       │   ├── AuthChallengeRepository.java
│   │                       │   ├── EventOutbox.java
│   │                       │   ├── PasswordHasher.java
│   │                       │   ├── RefreshTokenFactory.java
│   │                       │   ├── SessionRepository.java
│   │                       │   └── UserClock.java
│   │                       ├── security
│   │                       │   └── RateLimiterService.java
│   │                       ├── Argon2PasswordHasher.java
│   │                       ├── AuditService.java
│   │                       ├── AuthResult.java
│   │                       ├── AuthService.java
│   │                       ├── EmailVerificationService.java
│   │                       ├── HealthService.java
│   │                       ├── IdempotencyService.java
│   │                       ├── InMemoryRateLimitStore.java
│   │                       ├── JwtKeyRing.java
│   │                       ├── JwtKidResolver.java
│   │                       ├── JwtTokenService.java
│   │                       ├── LedgerService.java
│   │                       ├── LoginPolicy.java
│   │                       ├── MarketDataService.java
│   │                       ├── NotificationService.java
│   │                       ├── OrderCommandService.java
│   │                       ├── OrderQueryService.java
│   │                       ├── OutboxDispatcher.java
│   │                       ├── OutboxEvent.java
│   │                       ├── OutboxEventPublisher.java
│   │                       ├── PasswordPolicy.java
│   │                       ├── PortfolioService.java
│   │                       ├── RateLimitDecision.java
│   │                       ├── RateLimitStore.java
│   │                       ├── RedisRateLimitStore.java
│   │                       ├── RiskService.java
│   │                       ├── SecurityEventService.java
│   │                       ├── SessionService.java
│   │                       ├── SettlementService.java
│   │                       ├── TokenPolicy.java
│   │                       ├── TradingMetrics.java
│   │                       └── TradingService.java
│   └── pom.xml
├── .gitignore
├── FILE-MANIFEST.md
├── README.md
├── STRUCTURE.md
├── build.sh
├── docker-compose.yml
└── pom.xml

Total files: 690
Java files: 657
Java source lines: 28,116
