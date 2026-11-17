---
applyTo: "src/test/java/**/*.java"
description: "Map of the implemented tests and the files to open before changing or adding tests"
---
# Testing

Authoritative directory: `src/test/java/powerband/`. JUnit 5. Name tests `<ClassName>Test.java`. Run with `mvn test`.

## Implemented
- `StorageTest`: fixed record sizes, name validation, external merge sort over many sizes, binary search.
- `RecordingFlowTest`: record, save, load and play with the recorded intervals (60 ms tolerance), duplicate name, discard, double playback. Uses a fake OscClient that overrides `send`.
- `instrument/InstrumentsTest`: pitch detector, bass instrument, gamepad (edges, minor, octave) and phone (noteOn/noteOff, invalid messages), without any hardware.
- `MainTest`: startup message.
