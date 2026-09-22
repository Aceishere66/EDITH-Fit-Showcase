# EDITH Fit — Engineering Showcase

Curated engineering showcase of **EDITH Fit**, a personal Android health, wearable and workout-data platform developed within EDITH Dev Studio.

> This is a portfolio-oriented edition of a larger private project. It exposes selected architecture, real-device validation and representative source while excluding personal health data, private backend configuration and unrelated application code.

## Engineering problem

Health and wearable data is heterogeneous: different providers expose different schemas, provenance, update semantics and device metadata.

EDITH Fit is designed to collect, normalize, persist and synchronize that data while preserving where it came from and avoiding duplicate or ambiguous state.

## Stack

- Kotlin / Android
- Health Connect
- Samsung Health Data SDK validation
- WorkManager
- Room
- Supabase / PostgreSQL
- Wear OS
- Kotlin coroutines / Flow
- automated unit testing

## Implemented and verified areas

### Health Connect local reader

Physically verified support for metrics including activity, workouts, heart rate, resting heart rate, body measurements, sleep/stages and SpO2, with source provenance retained.

### Persistent cloud synchronization

The project implements:

- historical import
- incremental Health Connect change synchronization
- idempotent upserts
- ordered UPSERT/DELETE processing
- token/state handling
- foreground/manual/background sync coordination
- cancellation propagation
- Row Level Security in the cloud data model

### Samsung Health validation

The official Samsung Health Data SDK was tested on physical Samsung phone/watch hardware for selected richer telemetry including body composition, energy score, sleep and skin temperature.

The validation also investigated whether Samsung record identifiers survive through Health Connect; on tested records, timestamps aligned but a direct identifier bridge was not observed.

### Phone ↔ Wear OS state synchronization

The workout architecture includes an explicit shared protocol and revisioned phone/watch state publication model.

## Why provenance matters

EDITH Fit does not flatten every observation into an anonymous metric.

A normalized record can preserve information such as:

- originating application
- device manufacturer/model
- source identifier
- timestamps
- recording metadata

That makes debugging, deduplication and later analysis substantially safer.

## Representative source

- `samples/PhoneWearSyncCoordinator.kt` — revisioned state publication and serialized snapshot generation
- `samples/ProvenanceMappingTest.kt` — focused example showing source/device provenance preservation

## Documentation

- [Data and synchronization architecture](docs/DATA_PIPELINE.md)
- [Real-device validation](docs/REAL_DEVICE_VALIDATION.md)
- [Wear synchronization](docs/WEAR_SYNC.md)
- [Public showcase scope](docs/PUBLIC_SCOPE.md)

## Related

- Engineering portfolio: https://github.com/Aceishere66/engineering-portfolio
- EDITH Dev Studio engineering page: https://edithdevstudio.com/engineering
