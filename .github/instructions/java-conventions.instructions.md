---
applyTo: "src/main/java/**/*.java"
description: "Java conventions and package layout for Power Band (Maven layout)"
---
- Use Java only. Target the JDK version declared in `pom.xml`.
- The entrypoint (`public static void main`) stays in `src/main/java/powerband/Main.java`.
- Packages under `src/main/java/powerband/`: `constants`, `exception`, `instrument`, `command`, `synth`, `model`, `repository`, `sort`, `service`, `network`, `ui`.
- Packages are lowercase; classes are `PascalCase`; methods and fields are `camelCase`; constants are `UPPER_SNAKE_CASE`.
- No hardcoded values: every number, string, port, path, record size and synth name lives in the `constants` package.
- One public top-level type per file, and the file name matches the type name.
- Depend on interfaces for services and repositories; keep fields `private` and expose behavior through methods.
- Prefer inheritance and polymorphism over type checks. Use custom exceptions and try-with-resources.
- Prefer the standard library. Add a dependency only when the standard library cannot do the job.
