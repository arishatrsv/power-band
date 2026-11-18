---
applyTo: "src/main/java/**/*.java,PowerBandPhone/app/src/main/java/**/*.java"
description: "Map of the implemented frontend and the files to open before editing UI code"
---
# Frontend

Authoritative UI directories:
- `src/main/java/powerband/ui/`: desktop/console editor.
- `PowerBandPhone/app/src/main/java/cr/powerband/phone/`: Android external-organ instrument.
- `PowerBandPhone/app/build.gradle`: Android app build and SDK settings.

## Implemented
- `powerband.Main`: entrypoint `startupMessage()` and `main(String[])`; creates the bus, instruments, recorder/player, monitor, and console menu.
- `powerband.ui.Menu`: console editor with `Menu(Recorder, Player, Recordings, AtomicBoolean)` and `run()`; provides start/stop recording, list/play, stop playback, monitor toggle, and exit.
- `cr.powerband.phone.MainActivity`: programmatic Android UI plus accelerometer/touch input. Lifecycle methods `onCreate(Bundle)`, `onResume()`, `onPause()`, and `onDestroy()`; sensor callbacks `onSensorChanged(SensorEvent)` and `onAccuracyChanged(Sensor, int)`. It sends `x;y;z;touch` UDP datagrams to `9000`.

Before editing UI text or menu options, open `powerband.constants.Constants` and use its constants. The Android app currently contains hardcoded display text and network constants inside `MainActivity`; if adding user-facing text to that app, keep it consistent with project conventions and avoid duplicating protocol values.
