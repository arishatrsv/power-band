---
applyTo: "src/test/java/**/*.java"
description: "Map of the implemented tests and the files to open before changing or adding tests"
---
# Testing

Authoritative directory: `src/test/java/powerband/`. JUnit 5 is the test project. Run `mvn test`; package/verify with `mvn clean package`.

## Implemented
- `powerband.MainTest`: verifies `Main.startupMessage()` is not blank.
- `powerband.StorageTest`: fixed 48-byte/32-byte record sizes and round trips, corrupt-record rejection, name validation, merge-sort sizes `0`–`257`, binary search, chronology, and case-insensitive lookup.
- `powerband.RecordingFlowTest`: record/save/load/play flow, recorded timing with a 60 ms tolerance, duplicate names and pending-save retries, discard behavior, ignored commands outside capture, unknown names, and rejecting double playback. `FakeOsc` overrides protected `OscClient.send(...)`.
- `powerband.instrument.InstrumentsTest`: pitch detection/noise rejection, microphone onset and bass range, gamepad press-edge/minor/octave behavior, and phone touch/release/note/timbre plus bad-message handling.

Add tests in mirrored package directories and name classes `<ClassName>Test`. Use `@TempDir` for file-storage tests and fake devices/OSC implementations instead of hardware or a live SuperCollider process. Recordings tests must retain fixed-size and disk-search/sort behavior.
