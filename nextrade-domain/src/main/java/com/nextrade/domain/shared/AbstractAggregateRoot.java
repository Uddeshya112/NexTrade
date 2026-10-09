package com.nextrade.domain.shared;
import com.nextrade.common.event.DomainEvent; import com.nextrade.common.identifier.Identifier; import java.time.Instant; import java.util.*;
public abstract class AbstractAggregateRoot<ID extends Identifier<?>> implements AggregateRoot<ID> {
 private final ID id; private final Instant createdAt; private Instant updatedAt; private long revision; private final List<DomainEvent> events=new ArrayList<>();
 protected AbstractAggregateRoot(ID id){this(id,Instant.now(),Instant.now(),0);}
 protected AbstractAggregateRoot(ID id,Instant createdAt,Instant updatedAt,long revision){this.id=Objects.requireNonNull(id);this.createdAt=Objects.requireNonNull(createdAt);this.updatedAt=Objects.requireNonNull(updatedAt);this.revision=revision;}
 protected void touch(){updatedAt=Instant.now();revision++;}
 protected void emit(DomainEvent e){events.add(Objects.requireNonNull(e));}
 public final ID id(){return id;} public final Instant createdAt(){return createdAt;} public final Instant updatedAt(){return updatedAt;} public final long revision(){return revision;}
 public final List<DomainEvent> drainEvents(){List<DomainEvent> r=List.copyOf(events);events.clear();return r;}
}
