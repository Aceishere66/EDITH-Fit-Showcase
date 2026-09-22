# Real-Device Validation

A major engineering rule in EDITH Fit is that platform and wearable behavior must be tested on real hardware when an emulator or unit test cannot prove the behavior.

## Samsung Health Data SDK

A validation phase integrated Samsung Health Data SDK v1.1.0 and physically tested selected records on a Samsung Galaxy S25 and Galaxy Watch 6.

Validated examples included:

- body composition
- energy score
- sleep and sleep stages
- skin-temperature time series
- device provenance
- incremental change pagination

## Samsung ↔ Health Connect correlation

A specific experiment compared Samsung-side and Health Connect representations of the same tested event.

Observed:

- timestamps could align
- Samsung-specific UIDs were not observed propagating into Health Connect metadata

The engineering conclusion was therefore conservative: do not assume a stable cross-provider identifier bridge when the tested data does not demonstrate one.

## Why this matters

This validation style is representative of the project:

1. define the exact interoperability question;
2. test it on physical devices;
3. record what is directly observed;
4. avoid generalizing beyond the evidence;
5. make architecture decisions from the verified behavior.
