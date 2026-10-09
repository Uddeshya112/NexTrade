# NexTrade Architecture

## Dependency direction

`api -> service -> engine/domain/common`; `persistence -> domain/common`; `contracts -> common`; `fx-client -> contracts`.

The matching engine is deterministic and serializes all commands per instrument lane. Partitioning uses `Math.floorMod(UUID.hashCode(), partitionCount)` so `Integer.MIN_VALUE` does not produce a negative lane.

## Order lifecycle

Order identity is created once. A stop order changes its type in-place when triggered; no replacement UUID is created. A triggered order therefore retains its order ID, client order ID, audit sequence and lifecycle history.

## Money movement

Placement reserves cash/position through a risk/application service. Matching emits executions. Settlement consumes reservation exactly once and creates compensating ledger entries. The engine itself never reaches into persistence or publishes directly to Kafka.
