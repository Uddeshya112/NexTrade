package com.nextrade.engine.matching;

import com.nextrade.common.enumtype.*; import com.nextrade.common.identifier.*; import com.nextrade.common.valueobject.*; import com.nextrade.common.exception.*; import com.nextrade.domain.order.Order;
import java.time.*; import java.util.*; import java.util.concurrent.locks.ReentrantLock; import java.util.function.Consumer;

public final class OrderBook {
 private final InstrumentId instrumentId; private final Price tickSize; private final Consumer<TradeExecution> publisher; private final ReentrantLock lock=new ReentrantLock();
 private final NavigableMap<Price,Deque<Order>> bids=new TreeMap<>(Comparator.reverseOrder()); private final NavigableMap<Price,Deque<Order>> asks=new TreeMap<>();
 private final NavigableMap<Price,Deque<Order>> buyStops=new TreeMap<>(Comparator.reverseOrder()); private final NavigableMap<Price,Deque<Order>> sellStops=new TreeMap<>();
 private final Map<OrderId,OrderLocation> index=new HashMap<>(); private long sequence; private Price lastTradePrice;
 public OrderBook(InstrumentId id,Price tickSize,Consumer<TradeExecution> publisher){this.instrumentId=id;this.tickSize=tickSize;this.publisher=publisher==null?t->{}:publisher;}
 public List<TradeExecution> process(Order order){lock.lock();try{return processLocked(order,new TriggerContext());}finally{lock.unlock();}}
 private List<TradeExecution> processLocked(Order order,TriggerContext ctx){
  if(!order.instrument().id().equals(instrumentId))throw new InvalidOrderException("Wrong instrument");
  order.assignSequence(++sequence);
  if(order.type().isStop()){if(!StopTriggerPolicy.triggered(order.side(),lastTradePrice,order.stopPrice())){registerStop(order);return List.of();}order.trigger(Instant.now());}
  if(order.tif()==TimeInForce.FOK&&!canFullyFill(order)) {order.cancel("FOK cannot be fully filled");return List.of();}
  List<TradeExecution> trades=order.side()==OrderSide.BUY?matchBuy(order,ctx):matchSell(order,ctx);
  if(order.remaining().isPositive()&&order.active()){
    if(order.tif()==TimeInForce.IOC||order.tif()==TimeInForce.FOK)order.cancel(order.tif()+" could not be fully filled"); else if(order.type()==OrderType.MARKET)order.cancel("Market order exhausted available liquidity"); else addResting(order);
  }
  return trades;
 }
 private boolean canFullyFill(Order incoming){
  Quantity rem=incoming.remaining();
  NavigableMap<Price,Deque<Order>> levels=incoming.side()==OrderSide.BUY?asks:bids;
  for(var e:levels.entrySet()){
   if(!incoming.marketableAgainst(e.getKey()))break;
   for(Order resting:e.getValue()){
    // Self-trade prevention removes same-owner resting liquidity during matching.
    // Do not count that liquidity when deciding whether FOK can execute atomically.
    if(!resting.active()||resting.user().id().equals(incoming.user().id()))continue;
    Quantity q=rem.min(resting.remaining());
    rem=rem.subtract(q);
    if(rem.isZero())return true;
   }
  }
  return rem.isZero();
 }
 private List<TradeExecution> matchBuy(Order buy,TriggerContext ctx){List<TradeExecution> out=new ArrayList<>();Iterator<Map.Entry<Price,Deque<Order>>> it=asks.entrySet().iterator();while(it.hasNext()&&buy.remaining().isPositive()&&buy.active()){var e=it.next();if(!buy.marketableAgainst(e.getKey()))break;Deque<Order> q=e.getValue();while(!q.isEmpty()&&buy.remaining().isPositive()&&buy.active()){Order sell=q.peekFirst();if(sell.user().id().equals(buy.user().id())){sell.cancel("Self-trade prevention");q.pollFirst();index.remove(sell.id());continue;}Quantity qty=buy.remaining().min(sell.remaining());TradeExecution t=execute(buy,sell,qty,e.getKey());out.add(t);if(sell.status().isTerminal()){q.pollFirst();index.remove(sell.id());}triggerStops(t.price(),out,ctx);}if(q.isEmpty())it.remove();}return out;}
 private List<TradeExecution> matchSell(Order sell,TriggerContext ctx){List<TradeExecution> out=new ArrayList<>();Iterator<Map.Entry<Price,Deque<Order>>> it=bids.entrySet().iterator();while(it.hasNext()&&sell.remaining().isPositive()&&sell.active()){var e=it.next();if(!sell.marketableAgainst(e.getKey()))break;Deque<Order> q=e.getValue();while(!q.isEmpty()&&sell.remaining().isPositive()&&sell.active()){Order buy=q.peekFirst();if(buy.user().id().equals(sell.user().id())){buy.cancel("Self-trade prevention");q.pollFirst();index.remove(buy.id());continue;}Quantity qty=sell.remaining().min(buy.remaining());TradeExecution t=execute(buy,sell,qty,e.getKey());out.add(t);if(buy.status().isTerminal()){q.pollFirst();index.remove(buy.id());}triggerStops(t.price(),out,ctx);}if(q.isEmpty())it.remove();}return out;}
 private TradeExecution execute(Order buy,Order sell,Quantity qty,Price price){TradeId id=TradeId.generate();long seq=++sequence;buy.fill(qty,price,id);sell.fill(qty,price,id);lastTradePrice=price;TradeExecution t=new TradeExecution(id,buy.id(),sell.id(),buy.user().id(),sell.user().id(),instrumentId,qty,price,buy.type(),seq,Instant.now());publisher.accept(t);return t;}
 private void triggerStops(Price price,List<TradeExecution> accumulator,TriggerContext ctx){
  if(ctx.depth>=64)return;
  ctx.depth++;
  try {
   triggerSide(buyStops,price,true,accumulator,ctx);
   triggerSide(sellStops,price,false,accumulator,ctx);
  } finally {
   ctx.depth--;
  }
 }
 private void triggerSide(NavigableMap<Price,Deque<Order>> stops,Price price,boolean buy,List<TradeExecution> acc,TriggerContext ctx){List<Price> hits=new ArrayList<>();for(Price stopPrice:stops.keySet()){boolean hit=buy?stopPrice.compareTo(price)<=0:stopPrice.compareTo(price)>=0;if(hit)hits.add(stopPrice);}hits.sort(buy?Comparator.naturalOrder():Comparator.reverseOrder());for(Price stopPrice:hits){Deque<Order> q=stops.remove(stopPrice);if(q==null)continue;while(!q.isEmpty()){Order stop=q.pollFirst();index.remove(stop.id());if(!StopTriggerPolicy.triggered(stop.side(),price,stop.stopPrice()))continue;stop.trigger(Instant.now());acc.addAll(processLocked(stop,ctx));}}}
 private void registerStop(Order o){Price p=o.stopPrice();var map=o.side()==OrderSide.BUY?buyStops:sellStops;map.computeIfAbsent(p,k->new ArrayDeque<>()).addLast(o);index.put(o.id(),new OrderLocation(p,true));}
 private void addResting(Order o){if(o.limitPrice()==null)throw new InvalidOrderException("Resting order requires limit price");var map=o.side()==OrderSide.BUY?bids:asks;map.computeIfAbsent(o.limitPrice(),k->new ArrayDeque<>()).addLast(o);index.put(o.id(),new OrderLocation(o.limitPrice(),false));}
 public boolean cancel(OrderId id){lock.lock();try{OrderLocation loc=index.remove(id);if(loc==null)return false;var map=loc.stop()? (findStopMap(id,loc.price())) : (findBookMap(id,loc.price()));if(map==null)return false;Deque<Order> q=map.get(loc.price());if(q==null)return false;Order found=q.stream().filter(o->o.id().equals(id)).findFirst().orElse(null);if(found==null)return false;boolean removed=q.remove(found);if(removed)found.cancel("User cancel");if(q.isEmpty())map.remove(loc.price());return removed;}finally{lock.unlock();}}
 private NavigableMap<Price,Deque<Order>> findStopMap(OrderId id,Price p){return buyStops.containsKey(p)&&contains(buyStops.get(p),id)?buyStops:sellStops;}
 private NavigableMap<Price,Deque<Order>> findBookMap(OrderId id,Price p){return bids.containsKey(p)&&contains(bids.get(p),id)?bids:asks;}
 private boolean contains(Deque<Order> q,OrderId id){return q.stream().anyMatch(o->o.id().equals(id));}
 public Optional<Order> find(OrderId id){lock.lock();try{OrderLocation loc=index.get(id);if(loc==null)return Optional.empty();var map=loc.stop()?findStopMap(id,loc.price()):findBookMap(id,loc.price());Deque<Order> q=map.get(loc.price());return q==null?Optional.empty():q.stream().filter(o->o.id().equals(id)).findFirst();}finally{lock.unlock();}}
 public OrderBookSnapshot snapshot(int depth){lock.lock();try{return new OrderBookSnapshot(levels(bids,depth),levels(asks,depth),Optional.ofNullable(lastTradePrice),bids.isEmpty()?Optional.empty():Optional.of(bids.firstKey()),asks.isEmpty()?Optional.empty():Optional.of(asks.firstKey()),sequence);}finally{lock.unlock();}}
 private List<PriceLevel> levels(NavigableMap<Price,Deque<Order>> map,int depth){List<PriceLevel> out=new ArrayList<>();for(var e:map.entrySet()){if(out.size()>=depth)break;Quantity q=e.getValue().stream().filter(o->o.active()).map(Order::remaining).reduce(Quantity.zero(),Quantity::add);if(q.isPositive())out.add(new PriceLevel(e.getKey(),q,(int)e.getValue().stream().filter(Order::active).count()));}return List.copyOf(out);}
 public Optional<Price> lastTradePrice(){return Optional.ofNullable(lastTradePrice);} public long sequence(){return sequence;} public int orderCount(){return index.size();}
 public void seedLastTradePrice(Price price){lock.lock();try{lastTradePrice=price;}finally{lock.unlock();}}
 private record OrderLocation(Price price,boolean stop){} private static final class TriggerContext{int depth;}
}
