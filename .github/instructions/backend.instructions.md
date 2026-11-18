---
applyTo: "src/main/java/**/*.java,PowerBandPhone/app/src/main/java/**/*.java"
description: "Map of the implemented backend and the files to open before editing service or domain code"
---
# Backend

Authoritative directories under `src/main/java/powerband/`: `instrument`, `command`, `synth`, `service`, `model`, `network`, `exception`, `constants`. `Main.java` is the entrypoint. Respect the Bridge and command-bus rules in `.github/copilot-instructions.md`.

## Implemented
- `Main`: `startupMessage()`, `main(String[])`; starts OSC and three instruments, registers monitor/recorder/sound listeners, and opens `Menu`.
- `constants.Constants`: central numbers, paths, ports, OSC tags, synth names, UI text, and protocol values. It has no public methods.
- `exception.PowerBandException`: checked base exception with `PowerBandException(String)` and `PowerBandException(String, Throwable)`.
- `exception.DuplicateNameException(String)`: reports duplicate recording names.
- `model.Action`: enum `PLAY`, `NOTE_ON`, `NOTE_OFF`; `code()` and `fromCode(byte)`.
- `model.SynthType`: enum `PIANO`, `BASS`, `ORGAN`; `code()` and `fromCode(byte)`.
- `model.Params(Action, int, int, int)`: static `piano(int, int, boolean)`, `bass(int, int)`, `organOn(int, int)`, `organOff(int)`.
- `model.CommandRecord(long, SynthType, Params)`, `Header(String, long, long, int)`, and `NameEntry(String, long)`; `Header.placeholder(long)` creates an empty header.
- `command.SynthCommand`: `type()`, `execute(OscClient, Params)`, and static `of(SynthType)`; implementations `PianoCommand`, `BassCommand`, `OrganCommand` execute their synth behavior.
- `command.CommandBus`: nested `Listener.onCommand(SynthCommand, Params)`; public `subscribe(Listener)` and `publish(SynthCommand, Params)`. Publishing is thread-safe and isolates listener failures.
- `instrument.Instrument`: abstract Bridge abstraction; public `start()` and `stop()`, and `name()`; subclasses implement protected `open()`/`close()` and call protected `emit(Params)`.
- `instrument.GamepadInstrument`: constructor `GamepadInstrument(CommandBus, SynthCommand)`; JInput edge-detection piano, scale, octave, and minor/major behavior.
- `instrument.HeadphoneInstrument`: constructor `HeadphoneInstrument(CommandBus, SynthCommand)`; microphone onset/pitch detection and bass emission. Package-visible test seams include `onBlock(double[], float)` and `foldIntoBassRange(int)`.
- `instrument.PhoneInstrument`: constructor `PhoneInstrument(CommandBus, SynthCommand)`; UDP server on port `9000`, with package-visible `handle(String)` protocol parser.
- `instrument.PitchDetector`: static `detect(double[], float)` and `hzToMidi(double)`; private autocorrelation implementation.
- `instrument.GamepadCalibration`: standalone JInput diagnostics entrypoint `main(String[])`.
- `synth.OscClient`: constructor `OscClient()` or `OscClient(String, int)`; public `play(String, Object...)`, `startNote(int, String, Object...)`, `stopNote(int)`, `stopAll()`, `close()`; protected `send(String, Object...)` is the test seam. OSC state is synchronized.
- `service.Recorder`: constructor `Recorder(Recordings)`; synchronized `start()`, `onCommand(SynthCommand, Params)`, `stop()`, `save(String)`, `discard()`, `isCapturing()`, `isPendingSave()`.
- `service.Player`: constructor `Player(Recordings, OscClient)`; synchronized `play(String)`, `stop()`, `isPlaying()`; playback uses nanosecond absolute targets and daemon reader/player threads.
- `network`: no Java implementation currently exists. Android UDP networking is in `cr.powerband.phone.MainActivity` and acts as the external phone client.

Keep every emitted command on `CommandBus`; do not publish directly to OSC or storage. Shared instrument/OSC/recorder state must remain thread-safe.
