package com.nextrade.domain.shared;
import com.nextrade.common.identifier.Identifier;
import java.time.Instant;
public interface Entity<ID extends Identifier<?>> { ID id(); Instant createdAt(); Instant updatedAt(); long revision(); }
