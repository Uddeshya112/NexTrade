package com.nextrade.engine.risk; public interface RiskRule {RiskDecision evaluate(RiskContext context);String name();}
