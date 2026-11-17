---
applyTo: "**/*.{yml,yaml,xml,gradle,sh,bat,ps1,properties}"
description: "Map of the build and run setup and the files to open before changing it"
---
# Deploy

Build file: `pom.xml` (Maven, JUnit 5). `mvn clean package` produces `target/power-band.jar`, copies the dependencies to `target/lib/` (the jar manifest points there) and unpacks the JInput native libraries to `target/natives/`.
Run: `java -Djava.library.path=target/natives -jar target/power-band.jar`. JInput (with its `natives-all` classifier) is the only external library.
SuperCollider must be running with `supercollider/myband.sc` loaded (`~myband.load;`) before the app can make sound.
Do not change the build setup without checking `.github/copilot-instructions.md`.
