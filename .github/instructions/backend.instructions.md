---
applyTo: "src/main/java/**/*.java"
description: "Map of the implemented backend and the files to open before editing service or domain code"
---
# Backend

Authoritative directories under `src/main/java/powerband/`: `instrument`, `command`, `synth`, `service`, `model`, `network`, `exception`, `constants`.
Only the entrypoint `Main.java` exists. Run `update-current-development` after each feature to refresh this map.
Open only the files tied to the behavior area you are changing. Respect the Bridge pattern and the command bus rules in `.github/copilot-instructions.md`.
