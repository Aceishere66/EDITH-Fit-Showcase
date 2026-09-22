# Data and Synchronization Architecture

## High-level flow

```text
Wearables / health apps
          │
          ▼
     Health Connect
          │
          ▼
      EDITH Fit
     ┌────┴─────┐
     ▼          ▼
 Local state   Normalization
     │          │
     └────┬─────┘
          ▼
   Sync orchestration
          │
          ▼
 Supabase / PostgreSQL
```

## Engineering principles

### Preserve provenance

Source application and device metadata are retained where available instead of being discarded during normalization.

### Idempotent synchronization

Repeated synchronization should converge on the same logical state rather than create duplicates.

### Incremental changes

The architecture uses change tokens/state so normal synchronization does not require full re-imports.

### Explicit delete handling

Remote state is not treated as append-only. Change processing accounts for UPSERT and DELETE semantics.

### Coordination

Manual, foreground and scheduled synchronization paths are coordinated to avoid unnecessary concurrent work.

### Physical validation

APIs involving Health Connect, Samsung Health or wearable behavior are not considered fully verified from unit tests alone. Device tests are part of the engineering process.
