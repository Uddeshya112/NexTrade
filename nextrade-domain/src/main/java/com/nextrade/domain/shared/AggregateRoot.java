package com.nextrade.domain.shared;
import com.nextrade.common.event.DomainEvent; import com.nextrade.common.identifier.Identifier; import java.util.List;
public interface AggregateRoot<ID extends Identifier<?>> extends Entity<ID> { List<DomainEvent> drainEvents(); }
