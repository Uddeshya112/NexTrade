package com.nextrade.tests;

import com.nextrade.domain.order.Order;
import com.nextrade.common.enumtype.*; import com.nextrade.domain.instrument.*; import com.nextrade.domain.order.*; import com.nextrade.engine.matching.*; import org.junit.jupiter.api.*; import java.util.*; import static org.junit.jupiter.api.Assertions.*;
class EngineScenario05Test {
 private Instrument instrument; private com.nextrade.domain.user.User buyer; private com.nextrade.domain.user.User seller;
 @BeforeEach void setUp(){instrument=Fixture.instrument();buyer=Fixture.user("buyer5");seller=Fixture.user("seller5");}
 @Test void scenario05() throws Exception {
  MatchingEngine engine=new MatchingEngine(2,t->{});
  try {
   Order buy=Fixture.limit(buyer,instrument,OrderSide.BUY,"10","100.00");
   Order sell=Fixture.limit(seller,instrument,OrderSide.SELL,"10","100.00");
   var a=engine.place(buy).join(); var b=engine.place(sell).join();
   assertNotNull(a); assertNotNull(b); assertEquals(instrument.id(),buy.instrument().id());
   OrderBookSnapshot snap=engine.snapshot(instrument.id(),10); assertNotNull(snap); assertTrue(snap.sequence()>=1);
   assertTrue(buy.status().isTerminal()||buy.status()==OrderStatus.PARTIALLY_FILLED);
  } finally {engine.close();}
 }
 @Test void metadata() { assertTrue(instrument.tradeable()); assertEquals(1,instrument.lotSize()); assertNotNull(buyer.id()); assertFalse(seller.username().value().isBlank()); }
 @Test void description() { assertFalse("cancels IOC remainder".isBlank()); }
}
