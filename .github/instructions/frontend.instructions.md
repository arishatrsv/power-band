---
applyTo: "src/main/java/**/*.java"
description: "Map of the implemented frontend and the files to open before editing UI code"
---
# Frontend

Authoritative directory: `src/main/java/powerband/ui/` (console interface).

## Implemented
- `ui/Menu`: console menu of the editor (start recording, stop and name, load and play, stop playback, toggle the live monitor, exit). Option codes, prompts and messages come from `Constants`. `run()` loops until exit.
- The live monitor line is printed by the subscriber that `Main` registers on the `CommandBus`.
