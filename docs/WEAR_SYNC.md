# Phone ↔ Wear OS Synchronization

EDITH Fit uses an explicit synchronization boundary between the Android phone application and the Wear OS companion.

## Structure

```text
:wearprotocol
    shared DTOs
    versioning
    serialization
    validation
        │
        ├──────────────┐
        ▼              ▼
      :app            :wear
     phone           watch
```

The protocol module is kept separate from Android UI concerns so wire-level state remains explicit and testable.

## Revisioned state

The phone publishes an authoritative workout snapshot with a monotonic revision.

A snapshot can include:

- session ID/state
- current exercise
- exercise position/count
- current set
- completed sets
- rest-timer state
- workout preferences
- publication timestamp

Publication is serialized through a mutex so concurrent state changes do not race through the publisher.

## Failure semantics

A publication attempt distinguishes:

- published snapshot
- publication failure after authoritative revision allocation

This preserves an explicit state transition even when transport delivery is temporarily unsuccessful.

## Engineering relevance

The interesting part is not the workout domain itself; it is the state-distribution problem:

- explicit protocol boundary
- authoritative source
- monotonic revisions
- serialized publication
- asynchronous streams
- transport failure handling
