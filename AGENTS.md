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

## Project Structure

* `src/main`: Shared source code.
* `src/main/kotlin/cloud/glitchdev/rfu/achievement`: Achievement system core.
* `src/main/kotlin/cloud/glitchdev/rfu/data`: Data models and persistence.
* `versions/`: Version-specific overrides/configurations managed by Stonecutter.
* `processor/`: KSP processor module.
* `build.gradle.kts`: Main build script.

## Coding Conventions & Architecture

### Achievements

The mod features a robust achievement system with automatic registration and persistence.

#### Creating an Achievement

To create a new achievement, create a Kotlin `object` and annotate it with `@Achievement`. This class should be located
inside the `cloud.glitchdev.rfu.feature.achievement.achievements` package. Choose the appropriate base class:

1.  **`BaseAchievement`**: For simple, one-time tasks or custom logic.
2.  **`NumericAchievement`**: For counting tasks (e.g., "Catch 100 fish").
3.  **`StageAchievement`**: For multi-step tasks (e.g., "Catch Fish A, then Fish B, then Fish C").

#### Required Fields

Each achievement MUST define:

*   **`id`**: Unique identifier (String).
*   **`name`**: Display name.
*   **`description`**: Display description.
*   **`type`**: `AchievementType` (NORMAL, SECRET, HIDDEN).
*   **`difficulty`**: `AchievementDifficulty` (EASY to IMPOSSIBLE).
*   **`category`**: `AchievementCategory` (GENERAL, FISHING, COMBAT, etc.).

#### Progress Tracking

For progression to display correctly in the `AchievementWindow`:

*   **`NumericAchievement`**: Automatically uses `currentCount` and `targetCount`. Call `addProgress(amount)` to update.
*   **`StageAchievement`**: Automatically uses `currentStage` (starts at 1) and `targetStage`. Call `advanceStage()` to update.
Use addStageInfo() to add multiple names/descriptions. Achievement is completed when `currentStage > targetStage`
*   **`BaseAchievement`**: If you want to show numerical progress (e.g., "2/5"), override:
    ```kotlin
    override val currentProgress: Int get() = myValue
    override val targetProgress: Int get() = myGoal
    ```
    Also update `_progress = currentProgress.toFloat() / targetProgress.toFloat()` to drive the progress bar.

#### Implementation Workflow

1.  **Listeners**: Register event listeners in `setupListeners()`. The system automatically unregisters these when the achievement is completed.
2.  **Completion**: Call `complete()` to finish the achievement immediately.
3.  **Persistence**: For complex state (like a list of caught IDs), override `saveState()` and `loadState()`.

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

### Data Persistence

Preferred way to save data is using the `JsonFile` utility.

* **Usage:** Create a data class for your state and wrap it in a `JsonFile` instance.
* **Location:** `cloud.glitchdev.rfu.utils.JsonFile`.
* **Registration:** Ensure complex data types (like `Instant`) are handled in the `JsonFile`'s GsonBuilder if needed.

### Features

Features are the core units of functionality.

* **Definition:** Create a Kotlin `object` implementing the `Feature` interface.
* **Annotation:** Annotate the object with `@RFUFeature`.
* **Initialization:** Implement `onInitialize()` to register listeners/hooks.
* **Location:** `cloud.glitchdev.rfu.feature.*`

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

* **Managers:** Located in `cloud.glitchdev.rfu.events.managers`.
* **Usage:** `registerTickEvent`, `registerSeaCreatureCatchEvent`, etc.
* **runTasks** a function with this name should be used for running the events, it should
  use the `safeExecution {}` inside it to safely execute the events

### Configuration

Configuration is handled via `ResourcefulConfig`.

* **Access:** Settings are accessed via config objects (e.g., `GeneralFishing.fishingTime`).

### UI / HUD

* HUD elements should be separated from logic (e.g., `SCHDisplay`).
* Hud elements come from `AbstractHudElement` for their positioning/saving/scaling logic
* Uses `Elementa` for constructing UIs.

## Build Instructions

* **Build:** `./gradlew build`
* **Dependencies:** Managed via Gradle, with strict Maven repositories defined in `build.gradle.kts`.

## Important Notes

1. **Context Awareness:** Always check `src/main/kotlin` first, but be aware of `versions/` if dealing with
   version-specific bugs.
2. **Code Style:** Follow Kotlin idiomatic practices. Use `object` for singletons/features.
3. **Annotations:** Respect the `@RFUFeature` annotation; it's used by the `processor` to generate the loader code. If
   you add a new feature, ensure it's annotated so it gets loaded.
4. **Dependencies:** Do not assume standard Fabric API events are used directly everywhere; check for custom event
   managers first.
5. **Changelogs** Update the changelog after changes, if multiple changes can be compacted into one sentence, prefer
   using one sentence.
