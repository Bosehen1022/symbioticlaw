# Minecraft Forge Development Agent Rules

Before performing any action:

1. Read docs/V51_Specification.md
2. Follow Minecraft Forge 47.4.16
3. Do not add features not defined in the specification
4. Ensure all code compiles

Build pipeline:

gradlew clean build
gradlew runData
gradlew runGameTestServer

Requirements:

- Minecraft 1.20.1
- Forge 47.4.16
- Java 17
- Tick performance ≤ 50 ms