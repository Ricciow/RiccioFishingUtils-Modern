# Project Context: RiccioFishingUtils (RFU)

## Overview

RiccioFishingUtils is a Minecraft Mod for Hypixel SkyBlock, specifically focusing on fishing utilities. It is a
client-side mod built using Fabric and Kotlin.

## Key Technologies

* **Language:** Kotlin (primary), Java.
* **Loader:** Fabric Loader.
* **Multi-version Support:** Uses `Stonecutter` to manage multiple Minecraft versions (e.g., 1.21.10 - 1.21.11).
* **Libraries:**
    * **UniversalCraft / Elementa:** For UI/rendering (EssentialGG libs).
    * **ResourcefulConfig:** For configuration management.
    * **KSP (Kotlin Symbol Processing):** Used for code generation for feature, event, HUD, command, and achievement auto-discovery.

## System Architecture

The mod utilizes a KSP-driven auto-discovery system to minimize boilerplate.

### Auto-Discovery (KSP)

The `processor` module scans for specific annotations and generates `cloud.glitchdev.rfu.generated.RFULoader`. This loader is initialized in `RiccioFishingUtils.onInitializeClient`.

| Annotation | Interface/Base Class | Loader Function |
| :--- | :--- | :--- |
| `@RFUFeature` | `Feature` | `loadFeatures()` |
| `@Achievement` | `BaseAchievement` | `registerAchievements()` |
| `@Command` | `AbstractCommand` | `registerCommands()` |
| `@HudElement` | `AbstractHudElement` | `registerHud()` |
| `@AutoRegister` | `RegisteredEvent` | `registerEvents()` |
| `@InstantRegister` | `InstantRegisteredEvent` | `registerInstantEvents()` |

### Initialization Flow

1.  **Static Init**: `ConfigMigration` runs.
2.  **`onInitializeClient`**:
    - `RFULoader.registerInstantEvents()`
    - `CLIENT_STARTED` event registers the rest:
        - `loadFeatures()`
        - `registerCommands()`
        - `registerEvents()`
        - `registerHud()`
        - `registerAchievements()`

## Project Structure

* `src/main`: Shared source code.
* `src/main/kotlin/cloud/glitchdev/rfu/achievement`: Achievement system core.
* `src/main/kotlin/cloud/glitchdev/rfu/data`: Data models and persistence.
* `versions/`: Version-specific overrides/configurations managed by Stonecutter.
* `processor/`: KSP processor module.
* `build.gradle.kts`: Main build script.

## Achievement System

The mod features a robust achievement system with automatic registration and persistence.

### Creating an Achievement

To create a new achievement, create a Kotlin `object` and annotate it with `@Achievement`. This class should be located inside the `cloud.glitchdev.rfu.achievement.achievements` package. Choose the appropriate base class from `cloud.glitchdev.rfu.achievement.types`:

1.  **`BaseAchievement`**: For simple, one-time tasks or custom logic.
2.  **`NumericAchievement`**: For counting tasks (e.g., "Catch 100 fish"). Call `addProgress(amount)`.
3.  **`StageAchievement`**: For multi-step tasks (e.g., "Catch Fish A, then Fish B, then Fish C"). Call `advanceStage()`.
4.  **`NumericStageAchievement`**: For multi-step tasks that require a specific count per stage. Implement `getTargetCountForStage(stage: Int): Int`.

### Required Fields

Each achievement MUST define:
*   **`id`**: Unique identifier (String).
*   **`name`**: Display name.
*   **`description`**: Display description.
*   **`type`**: `AchievementType` (NORMAL, SECRET, HIDDEN).
*   **`difficulty`**: `AchievementDifficulty` (EASY, MEDIUM, HARD, VERY_HARD, IMPOSSIBLE).
*   **`category`**: `AchievementCategory` (GENERAL, ISLE, SPECIAL).

### Progress Tracking

*   **`NumericAchievement`**: Automatically uses `currentCount` and `targetCount`.
*   **`StageAchievement`**: Automatically uses `currentStage` (starts at 1) and `targetStage`. Use `addStageInfo()` to add metadata per stage.
*   **`BaseAchievement`**: To show numerical progress (e.g., "2/5"), override:
    ```kotlin
    override val currentProgress: Int get() = myValue
    override val targetProgress: Int get() = myGoal
    ```
    Also update `_progress = currentProgress.toFloat() / targetProgress.toFloat()` to drive the progress bar.

### Implementation Workflow

1.  **Listeners**: Register event listeners in `setupListeners()`. The system automatically unregisters these when the achievement is completed.
2.  **Completion**: Call `complete()` to finish the achievement immediately.
3.  **Persistence**: For complex state, override `saveState()` and `loadState(progressData: Map<String, Any>)`.

**Example:**

```kotlin
@Achievement
object SeaCreatureExpert : BaseAchievement() {
    override val id = "sc_expert"
    override val name = "Marine Biologist"
    override val description = "Catch 5 unique sea creatures."
    override val type = AchievementType.NORMAL
    override val difficulty = AchievementDifficulty.MEDIUM
    override val category = AchievementCategory.COLLECTION

    private val caughtIds = mutableSetOf<String>()
    private val goal = 5

    override val currentProgress: Int get() = caughtIds.size
    override val targetProgress: Int get() = goal

    override fun setupListeners() {
        activeListeners.add(registerSeaCreatureCatchEvent { sc, _ ->
            if (caughtIds.add(sc.id)) {
                _progress = currentProgress.toFloat() / targetProgress.toFloat()
                if (caughtIds.size >= goal) complete()
            }
        })
    }

    override fun loadState(progressData: Map<String, Any>) {
        (progressData["ids"] as? List<*>)?.forEach { if (it is String) caughtIds.add(it) }
        _progress = currentProgress.toFloat() / targetProgress.toFloat()
    }

    override fun saveState() = mapOf("ids" to caughtIds.toList())
}
```

## Command System

Commands use Mojang Brigadier but are wrapped for easier use and auto-registration.

### Creating a Command

Annotate a class with `@Command` and extend `AbstractCommand` or `SimpleCommand`.

*   **`AbstractCommand(name)`**: Provides DSL-like helpers:
    - `lit(string)`: Literal argument.
    - `arg(name, type)`: Required argument.
    - `subCommands`: List of sub-commands.
    - `build(builder)`: Define command logic.
*   **`SimpleCommand(name)`**: Provides a simplified `execute(context)` method.

### Custom Argument Types

*   **`StringListArgumentType`**: Takes a list of strings and provides suggestions.
    - `greedy`: Consumes the rest of the string.
    - `exclusive`: Only allows values from the provided list.

**Retrieving Arguments:**
```kotlin
val value = StringArgumentType.getString(context, "argName")
```

## Event Management

RFU uses a custom layer on top of Fabric events for "managed tasks".

### Event Managers (`cloud.glitchdev.rfu.events.managers`)

Managers are annotated with `@AutoRegister`. They must implement `RegisteredEvent` and use `runTasks` with `safeExecution {}`.

Common managers:
- `TickEvents`: `registerTickEvent(priority, interval) { ... }`
- `ChatEvents`: `registerChatEvent`, `registerGameEvent(regex)`.
- `SeaCreatureCatchEvents`: `registerSeaCreatureCatchEvent { creature, isLootshared -> ... }`

### Managed Tasks

Registration returns a `ManagedTask`. Call `.unregister()` to stop updates. Achievement base classes track these in `activeListeners` for auto-unregistration.

## Features

Features are core functionality units.

*   **Definition:** Kotlin `object` implementing `Feature`.
*   **Annotation:** `@RFUFeature`.
*   **Initialization:** Implement `onInitialize()` to register listeners.

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

## Back-end & Networking

*   **`Network` Object**: Handles async requests via Java's `HttpClient`.
*   **Authentication**: Uses Mojang session server. Tokens managed in `Network`.
*   **Acknowledgement**: Users must accept the back-end connection (`/rfubackend accept`).

## Data Persistence

Preferred method is the `JsonFile` utility (`cloud.glitchdev.rfu.utils.JsonFile`).

* **Usage:** Create a data class and wrap it in a `JsonFile` instance.
* **Registration:** Ensure complex data types (like `Instant`) are handled in `GsonBuilder` if needed.

## UI & HUD

*   **HUD Elements:** Annotate with `@HudElement` and extend `AbstractHudElement` for positioning/scaling.
*   **Framework:** Uses `Elementa` and `UIScheme` for styling.
*   **Logic vs View:** Render in `HudElement`, logic in `Feature` or manager.

## Utilities & Helpers

*   **`Chat`**: Helpers for messages, commands, and queued party messages.
*   **`Party`**: Tracks status via Hypixel Mod API and chat regex.
*   **`TextUtils`**: Prefixing with `[RFU]` or `[RFUPF]`.
*   **DSL Extensions**: Located in `cloud.glitchdev.rfu.utils.dsl`.

## Build & Workspace

*   **Build:** `./gradlew build`
*   **Stonecutter:** Use to switch Minecraft versions.

## Important Notes

1.  **Context Awareness:** Check `src/main/kotlin` first, but watch `versions/` for version-specific overrides.
2.  **Code Style:** Follow idiomatic Kotlin. Use `object` for singletons/features/managers.
3.  **Annotations:** Respect `@RFUFeature`, `@Achievement`, etc.; they are required for the `processor` to load the code.
4.  **Dependencies:** Use custom event managers (`cloud.glitchdev.rfu.events.managers`) instead of standard Fabric events where possible.
5.  **Changelogs:** Update the changelog after changes. Compact multiple changes into one sentence when possible.
