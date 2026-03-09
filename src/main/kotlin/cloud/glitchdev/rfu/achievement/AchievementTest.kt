package cloud.glitchdev.rfu.achievement

import cloud.glitchdev.rfu.achievement.types.StageAchievement
import cloud.glitchdev.rfu.events.managers.SeaCreatureCatchEvents.registerSeaCreatureCatchEvent
import cloud.glitchdev.rfu.gui.window.AchievementWindow
import cloud.glitchdev.rfu.utils.command.AbstractCommand
import cloud.glitchdev.rfu.utils.command.Command
import cloud.glitchdev.rfu.utils.gui.Gui
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.network.chat.Component

@Achievement
object StandaloneTestAchievement : BaseAchievement() {
    override val id = "test_standalone"
    override val name = "Standalone Explorer"
    override val description = "A simple achievement for testing purposes."
    override val type = AchievementType.NORMAL
    override val difficulty = AchievementDifficulty.EASY
    override val category = AchievementCategory.GENERAL

    override fun setupListeners() {
        // In a real scenario, you'd register an event listener here
        // For testing, we'll trigger it via command
    }

    fun trigger() {
        complete()
    }
}

@Achievement
object StageTestAchievement : StageAchievement() {
    override val id = "test_stage"
    override val name = "Stage Master"
    override val description = "Complete all 3 stages to earn this achievement."
    override val type = AchievementType.NORMAL
    override val difficulty = AchievementDifficulty.MEDIUM
    override val category = AchievementCategory.GENERAL
    override val targetStage = 3

    init {
        addStageInfo(1, "Stage Master: The Beginning", "Start your journey by completing the first stage.")
        addStageInfo(2, "Stage Master: Halfway There", "You've reached the second stage, keep going!")
        addStageInfo(3, "Stage Master: Final Challenge", "One last stage to become the Master.")
    }

    override fun setupListeners() {
        // Listeners for stage progression
    }
    
    fun progress() {
        advanceStage()
    }
}

@Achievement
object FishingTestAchievement : BaseAchievement() {
    override val id = "test_fishing"
    override val name = "Novice Fisherman"
    override val description = "Testing fishing category display."
    override val type = AchievementType.NORMAL
    override val difficulty = AchievementDifficulty.EASY
    override val category = AchievementCategory.FISHING

    override fun setupListeners() {}
}

@Achievement
object CombatTestAchievement : BaseAchievement() {
    override val id = "test_combat"
    override val name = "Warrior Spirit"
    override val description = "Testing combat category display."
    override val type = AchievementType.SECRET
    override val difficulty = AchievementDifficulty.HARD
    override val category = AchievementCategory.COMBAT

    override fun setupListeners() {}
}

@Achievement
object CollectionAchievement : BaseAchievement() {
    override val id = "collection_explorer"
    override val name = "Marine Biologist"
    override val description = "Catch 5 unique sea creatures."
    override val type = AchievementType.NORMAL
    override val difficulty = AchievementDifficulty.MEDIUM
    override val category = AchievementCategory.COLLECTION

    private val caughtSeaCreatures = mutableSetOf<String>()
    private val targetCount = 5

    override val currentProgress: Int get() = caughtSeaCreatures.size
    override val targetProgress: Int get() = targetCount

    override fun setupListeners() {
        activeListeners.add(registerSeaCreatureCatchEvent { sc, _ ->
            if (caughtSeaCreatures.add(sc.name)) {
                updateProgress()
            }
        })
    }

    private fun updateProgress() {
        _progress = caughtSeaCreatures.size.toFloat() / targetCount.toFloat()
        if (caughtSeaCreatures.size >= targetCount) {
            complete()
        }
    }

    override fun loadState(progressData: Map<String, Any>) {
        super.loadState(progressData)
        val savedCatches = progressData["caughtIds"] as? List<*>
        savedCatches?.forEach { id ->
            if (id is String) {
                caughtSeaCreatures.add(id)
            }
        }
        updateProgress()
    }

    override fun saveState(): Map<String, Any> {
        val state = super.saveState().toMutableMap()
        state["caughtIds"] = caughtSeaCreatures.toList()
        return state
    }
}

@Achievement
object CounterTestAchievement : cloud.glitchdev.rfu.achievement.types.NumericAchievement() {
    override val id = "test_counter"
    override val name = "Fish Counter"
    override val description = "Catch 10 fish to test numeric progression."
    override val type = AchievementType.NORMAL
    override val difficulty = AchievementDifficulty.EASY
    override val category = AchievementCategory.FISHING
    override val targetCount = 10

    override fun setupListeners() {}
    
    fun add() {
        addProgress(1)
    }
}

@Command
object AchievementTestCommand : AbstractCommand("rfutest") {
    override val description = "Commands for testing the achievement system"

    override fun build(builder: LiteralArgumentBuilder<FabricClientCommandSource>) {
        builder.then(
            lit("achievements")
                .then(
                    lit("open")
                        .executes { _ ->
                            Gui.openGui(AchievementWindow)
                            1
                        }
                )
                .then(
                    lit("list")
                        .executes { context ->
                            val achievements = AchievementProvider.getVisibleAchievements()
                            context.source.sendFeedback(Component.literal("§6--- Registered Achievements ---"))
                            achievements.forEach { ach ->
                                val status = if (ach.isCompleted) "§a[COMPLETED]" else "§e[PROGRESS: ${ach.currentProgress}/${ach.targetProgress} (${(ach.progress * 100).toInt()}%)]"
                                context.source.sendFeedback(Component.literal("§7- §f${ach.name} §7(${ach.id}) $status §8| Difficulty: ${ach.difficulty} §8| Category: ${ach.category}"))
                            }
                            1
                        }
                )
                .then(
                    lit("complete_standalone")
                        .executes { context ->
                            StandaloneTestAchievement.trigger()
                            context.source.sendFeedback(Component.literal("§aTriggered completion for Standalone achievement!"))
                            1
                        }
                )
                .then(
                    lit("progress_stage")
                        .executes { context ->
                            StageTestAchievement.progress()
                            context.source.sendFeedback(Component.literal("§aProgressed Stage achievement! Current stage: ${StageTestAchievement.currentStage}/${StageTestAchievement.targetStage}"))
                            1
                        }
                )
                .then(
                    lit("progress_counter")
                        .executes { context ->
                            CounterTestAchievement.add()
                            context.source.sendFeedback(Component.literal("§aProgressed Counter achievement! Progress: ${CounterTestAchievement.currentProgress}/${CounterTestAchievement.targetCount}"))
                            1
                        }
                )
                .then(
                    lit("trigger_catch")
                        .then(
                            arg("sc_index", IntegerArgumentType.integer())
                                .executes { context ->
                                    val index = IntegerArgumentType.getInteger(context, "sc_index")
                                    val sc = cloud.glitchdev.rfu.constants.SeaCreatures.entries.getOrNull(index)
                                    if (sc != null) {
                                        cloud.glitchdev.rfu.events.managers.SeaCreatureCatchEvents.runTasks(sc, false)
                                        context.source.sendFeedback(Component.literal("§aTriggered catch for ${sc.scName}!"))
                                    } else {
                                        context.source.sendFeedback(Component.literal("§cInvalid sea creature index!"))
                                    }
                                    1
                                }
                        )
                )
                .then(
                    lit("save")
                        .executes { context ->
                            AchievementManager.saveAll()
                            context.source.sendFeedback(Component.literal("§aAll achievements saved to disk!"))
                            1
                        }
                )
        )
    }
}
