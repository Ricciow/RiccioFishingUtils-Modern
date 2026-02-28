# Project Context: RiccioFishingUtils (RFU)

## Overview
RiccioFishingUtils is a Minecraft Mod for Hypixel SkyBlock, specifically focusing on fishing utilities. It is a client-side mod built using Fabric and Kotlin.

## Key Technologies
*   **Language:** Kotlin (primary), Java.
*   **Loader:** Fabric Loader.
*   **Multi-version Support:** Uses `Stonecutter` to manage multiple Minecraft versions (e.g., 1.21.10 - 1.21.11).
*   **Libraries:**
    *   **UniversalCraft / Elementa:** For UI/rendering (EssentialGG libs).
    *   **ResourcefulConfig:** For configuration management.
    *   **KSP (Kotlin Symbol Processing):** Used for code generation (likely for feature auto-discovery).

## Project Structure
*   `src/main`: Shared source code.
*   `versions/`: Version-specific overrides/configurations managed by Stonecutter.
*   `processor/`: KSP processor module.
*   `build.gradle.kts`: Main build script.

## Coding Conventions & Architecture

### Features
Features are the core units of functionality.
*   **Definition:** Create a Kotlin `object` implementing the `Feature` interface.
*   **Annotation:** Annotate the object with `@RFUFeature`.
*   **Initialization:** Implement `onInitialize()` to register listeners/hooks.
*   **Location:** `cloud.glitchdev.rfu.feature.*`

**Example:**
```kotlin
@RFUFeature
object MyFeature : Feature {
    override fun onInitialize() {
        registerTickEvent(interval = 20) {
            // Logic
        }
    }
}
```

### Events
The mod uses a functional event registration system.
*   **Managers:** Located in `cloud.glitchdev.rfu.events.managers`.
*   **Usage:** `registerTickEvent`, `registerSeaCreatureCatchEvent`, etc.

### Configuration
Configuration is handled via `ResourcefulConfig`.
*   **Access:** Settings are accessed via config objects (e.g., `GeneralFishing.fishingTime`).

### UI / HUD
*   HUD elements should be separated from logic (e.g., `SCHDisplay`).
*   Hud elements come from `AbstractHudElement` for their positioning/saving/scaling logic
*   Uses `Elementa` for constructing UIs.

## Build Instructions
*   **Build:** `./gradlew build`
*   **Dependencies:** Managed via Gradle, with strict Maven repositories defined in `build.gradle.kts`.

## Important Notes
1.  **Context Awareness:** Always check `src/main/kotlin` first, but be aware of `versions/` if dealing with version-specific bugs.
2.  **Code Style:** Follow Kotlin idiomatic practices. Use `object` for singletons/features.
3.  **Annotations:** Respect the `@RFUFeature` annotation; it's used by the `processor` to generate the loader code. If you add a new feature, ensure it's annotated so it gets loaded.
4.  **Dependencies:** Do not assume standard Fabric API events are used directly everywhere; check for custom event managers first.
5.  **Changelogs** Update the changelog after changes, if multiple changes can be compacted into one sentence, prefer using one sentence.
