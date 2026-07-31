# Minemons

Minemons is being reorganized into a multi-module Minecraft 1.20.1 project so shared resources can be packaged for both Fabric and Forge builds.

## Modules

- `common`: shared assets and data loaded by both distributions.
- `fabric`: existing Fabric implementation, entrypoints, networking, commands, UI, tutorial, and card-rendering code.
- `forge`: Forge loader scaffold with metadata and a Forge `@Mod` entrypoint.

## Build targets

Use the loader-specific Gradle tasks to build each distribution:

```bash
./gradlew :fabric:build
./gradlew :forge:build
```

The Fabric module currently preserves the existing Yarn/Fabric implementation. The Forge module is intentionally a scaffold for the loader split; Fabric-only gameplay, networking, tutorial hooks, and card rendering still need to be extracted behind common abstractions before feature parity is complete.
