---
applyTo: "**/*.{yml,yaml,xml,gradle,sh,bat,ps1,properties}"
description: "Map of the build and run setup and the files to open before changing it"
---
# Deploy

Build file: `pom.xml` (Maven, JUnit 5, jar plugin with main class `powerband.Main`). Output: `target/power-band.jar`.
SuperCollider must be running with `supercollider/myband.sc` loaded before the app can make sound.
Do not change the build setup without checking `.github/copilot-instructions.md`.
