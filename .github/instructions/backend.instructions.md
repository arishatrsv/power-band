---
applyTo: "src/main/java/**/*.java"
description: "Map of the implemented backend and the files to open before editing service or domain code"
---
# Backend

Authoritative directories under `src/main/java/powerband/`: `instrument`, `command`, `synth`, `service`, `model`, `exception`, `constants`. The entrypoint is `Main.java`.
Respect the Bridge pattern and the command bus rules in `.github/copilot-instructions.md`. Open only the files tied to the behavior area you are changing.

## Implemented
- `Main.java`: connects OscClient, subscribes sound, monitor and recorder to the CommandBus, starts the three instruments (a failing device is reported and skipped), opens the Menu, and on exit stops the player and instruments and discards any pending recording.
- `constants/Constants`: every number, text, port, path, record size and synth name.
- `exception/PowerBandException` (checked base) and `DuplicateNameException`.
- `model/`: records `Params(action, note, durMs, extra)`, `CommandRecord`, `Header`, `NameEntry`; enums `Action`, `SynthType` (with byte codes for the binary files).
- `synth/OscClient`: OSC over UDP to SuperCollider. `play`, `startNote`, `stopNote`, `stopAll`, `close`. Synchronized; `send` is protected so tests can replace it.
- `command/` (implementation side of the Bridge): `SynthCommand` (`type()`, `execute(osc, params)`, `of(SynthType)`), `PianoCommand`, `BassCommand`, `OrganCommand`, and `CommandBus` (`subscribe`, `publish`).
- `instrument/` (abstraction side of the Bridge): `Instrument` (`start`, `stop`, `emit`, `name`; subclasses implement `open`/`close`), `GamepadInstrument` (JInput, piano), `PhoneInstrument` (UDP server on the phone port, organ), `HeadphoneInstrument` (microphone, bass), `PitchDetector` (autocorrelation), `GamepadCalibration` (temporary tool).
- `service/Recorder` (bus listener: `start`, `stop`, `save`, `discard`) and `service/Player` (`play`, `stop`, `isPlaying`; reader thread + queue + absolute-time player thread).
