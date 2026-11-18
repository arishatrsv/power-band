---
applyTo: "**/*.{yml,yaml,xml,gradle,sh,bat,ps1,properties}"
description: "Map of the build and run setup and the files to open before changing it"
---
# Deploy

Primary build: `pom.xml` (Maven), Java release `21`, JUnit 5.11.4, JInput 2.0.10, Surefire, and dependency plugins.

- `mvn clean package` produces `target/power-band.jar`, copies runtime dependencies to `target/lib/`, and unpacks JInput natives to `target/natives/`.
- Run on macOS/Linux-style setup with `java -Djava.library.path=target/natives -jar target/power-band.jar`.
- The jar manifest main class is `powerband.Main` and classpath prefix is `lib/`.
- SuperCollider must run `supercollider/myband.sc` and load `~myband.load;` before OSC commands can produce sound.
- Android companion build: `PowerBandPhone/app/build.gradle`, Gradle Android plugin, `compileSdk 34`, `minSdk 24`, `targetSdk 34`, Java 8 source/target compatibility. Open `PowerBandPhone/settings.gradle`, `build.gradle`, `gradle.properties`, and `local.properties` for the complete Android build environment.

Do not change the Maven JAR layout or native extraction behavior without checking `.github/copilot-instructions.md`. The Android app is a separate UDP client and is not packaged into the Maven JAR.
