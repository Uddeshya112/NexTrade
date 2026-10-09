package com.nextrade.engine.matching;
import com.nextrade.common.identifier.*; import com.nextrade.common.valueobject.*; import com.nextrade.common.enumtype.*; import com.nextrade.domain.order.Order; import com.nextrade.common.exception.*; import java.util.*; import java.util.concurrent.*; import java.util.concurrent.atomic.*; import java.util.function.Consumer;
public final class MatchingEngine implements AutoCloseable{
 private final int partitions; private final ExecutorService[] lanes; private final Map<InstrumentId,OrderBook> books=new ConcurrentHashMap<>(); private final AtomicLong processed=new AtomicLong(); private final Consumer<TradeExecution> publisher;
 public MatchingEngine(int partitions,Consumer<TradeExecution> publisher){if(partitions<1)throw new IllegalArgumentException("partitions");this.partitions=partitions;this.publisher=publisher==null?t->{}:publisher;this.lanes=new ExecutorService[partitions];for(int i=0;i<partitions;i++){int n=i;lanes[i]=Executors.newSingleThreadExecutor(r->{Thread t=new Thread(r,"nextrade-engine-"+n);t.setDaemon(true);return t;});}}
 private int lane(InstrumentId id){return Math.floorMod(id.value().hashCode(),partitions);} private OrderBook book(InstrumentId id){return books.computeIfAbsent(id,k->new OrderBook(k,new Price(java.math.BigDecimal.valueOf(0.01),java.math.BigDecimal.valueOf(0.01)),publisher));}
 public CompletableFuture<MatchingResult> place(Order order){return submit(order.instrument().id(),()->{List<TradeExecution> trades=book(order.instrument().id()).process(order);processed.incrementAndGet();return new MatchingResult(order.id(),order.status(),trades,null);});}
 public CompletableFuture<Boolean> cancel(InstrumentId instrument,OrderId id){return submit(instrument,()->book(instrument).cancel(id));}
 public CompletableFuture<Optional<Order>> find(InstrumentId instrument,OrderId id){return submit(instrument,()->book(instrument).find(id));}
 public OrderBookSnapshot snapshot(InstrumentId instrument,int depth){return book(instrument).snapshot(depth);} public long processed(){return processed.get();}
 private <T> CompletableFuture<T> submit(InstrumentId id,Callable<T> task){CompletableFuture<T> f=new CompletableFuture<>();lanes[lane(id)].execute(()->{try{f.complete(task.call());}catch(Exception e){f.completeExceptionally(e);}});return f;}
 @Override public void close(){for(ExecutorService l:lanes){l.shutdown();try{if(!l.awaitTermination(5,TimeUnit.SECONDS))l.shutdownNow();}catch(InterruptedException e){l.shutdownNow();Thread.currentThread().interrupt();}}}
}
