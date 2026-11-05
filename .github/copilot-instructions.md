# Copilot project instructions

Project: Power Band. A Java application where three physical instruments drive three SuperCollider synths, and a music editor monitors, records, stores and replays the commands.

## Stack
- Java only. Target the JDK version declared in `pom.xml` (`maven.compiler.release`).
- Maven build. Tests with JUnit 5. Run tests with `mvn test`; package with `mvn package` (jar: `target/power-band.jar`).
- Sound is produced by SuperCollider (`supercollider/myband.sc`). Java talks to `scsynth` over OSC/UDP. `reference/BandClient.java.txt` is only a reference; the project code is a refactored version of it under `powerband.synth`.

## Layout
The entrypoint (`public static void main`) stays in `src/main/java/powerband/Main.java`.
- `constants/` — the constants library. No hardcoded numbers, strings, ports, paths, record sizes or synth names anywhere else.
- `exception/` — custom exceptions.
- `instrument/` — instrument hierarchy driven by physical devices (abstraction side of the Bridge pattern).
- `command/` — synth command hierarchy and the command bus (implementation side of the Bridge pattern).
- `synth/` — OSC/UDP connection to SuperCollider.
- `model/` — command record, recording header, index entry, list entry.
- `repository/` — binary files `recordings.music`, `recordings.index`, `recordings.list` and binary search on disk.
- `sort/` — external Balanced Merge Sort.
- `service/` — recorder, player, editor.
- `network/` — server for an external instrument (only if one is used).
- `ui/` — console or Swing interface (live monitor, menus).
Tests mirror these packages under `src/test/java/powerband/`.

## Role mapping for specs
- frontend: `ui`
- backend: `instrument`, `command`, `synth`, `service`, `model`, `network`, `exception`, `constants`
- database: `repository`, `sort` (file persistence, not SQL)
- testing: JUnit 5 under `src/test/java`
- deploy: `pom.xml`, jar packaging, run scripts

## Java conventions
- Packages are lowercase; classes are `PascalCase`; methods and fields are `camelCase`; constants are `UPPER_SNAKE_CASE`.
- Identifiers in English. Text shown to the user comes from the constants library.
- One public top-level type per file, and the file name matches the type name.
- Depend on interfaces for services and repositories; keep fields `private` and expose behavior through methods.
- Prefer the standard library. Add a dependency only when the standard library cannot do the job.
- Use inheritance and polymorphism instead of `if/switch` on types. Throw custom exceptions, not bare `RuntimeException`. Close streams with try-with-resources.

## Project rules that must hold in every feature
- Bridge pattern: the instrument hierarchy (abstraction) holds a reference to the synth command hierarchy (implementation). Neither hierarchy knows the concrete classes of the other.
- Every command, from an instrument or from playback, goes through one command bus. Listeners (monitor, recorder) subscribe to it.
- Recordings are stored in binary files with fixed-size records (`RandomAccessFile` or `DataOutputStream`/`DataInputStream`, fixed-width fields). Do not use `ObjectOutputStream`.
- Never load recordings into in-memory arrays or lists to search or sort them. Binary search and Balanced Merge Sort work directly on the files, record by record.
- Playback uses absolute target times from a single start instant, not accumulated `sleep` calls.
- Shared state touched by several instruments at once must be thread-safe.

For project-specific execution flow and layer instructions, follow the repository instructions under `.github/instructions/`.
