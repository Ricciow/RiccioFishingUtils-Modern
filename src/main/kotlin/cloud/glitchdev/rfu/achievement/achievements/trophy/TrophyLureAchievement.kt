package cloud.glitchdev.rfu.achievement.achievements.trophy

import cloud.glitchdev.rfu.RiccioFishingUtils.mc
import cloud.glitchdev.rfu.achievement.Achievement
import cloud.glitchdev.rfu.achievement.AchievementCategory
import cloud.glitchdev.rfu.achievement.AchievementDifficulty
import cloud.glitchdev.rfu.achievement.AchievementType
import cloud.glitchdev.rfu.achievement.BaseAchievement
import cloud.glitchdev.rfu.achievement.AchievementProvider
import cloud.glitchdev.rfu.events.managers.ContainerEvents.registerContainerOpenEvent
import cloud.glitchdev.rfu.events.managers.TickEvents.registerTickEvent
import cloud.glitchdev.rfu.utils.Tablist
import cloud.glitchdev.rfu.utils.dsl.removeFormatting
import gg.essential.universal.utils.toUnformattedString
import net.minecraft.core.component.DataComponents

@Achievement
object TrophyLureAchievement : BaseAchievement() {
    override val id: String = "trophy_lure"
    override val name: String = "Trophy Lure"
    override val description: String = "Reach 150 trophy chance."
    override val type: AchievementType = AchievementType.NORMAL
    override val difficulty: AchievementDifficulty = AchievementDifficulty.HARD
    override val category: AchievementCategory = AchievementCategory.TROPHY_FISHING

    override val targetProgress: Long get() = 150L
    override val currentProgress: Long get() = highestTrophyChance.toLong()

    private var highestTrophyChance: Float = 0.0f
        set(value) {
            field = value
            _progress = (value / 150.0f).coerceAtMost(1.0f)
        }

    private val tablistRegex = """Trophy Chance:\s*\s*([0-9]+(?:\.[0-9]+)?)""".toRegex(RegexOption.IGNORE_CASE)
    private val statRegex = """\s*Trophy Chance\s*([0-9]+(?:\.[0-9]+)?)%?""".toRegex(RegexOption.IGNORE_CASE)

    override fun setupListeners() {
        activeListeners.add(registerTickEvent(interval = 20) {
            checkProgress()
        })

        activeListeners.add(registerContainerOpenEvent { _, _ ->
            checkProgress()
        })
    }

    private fun extractTrophyChance(text: String, regex: Regex): Float? {
        val match = regex.find(text) ?: return null
        return match.groupValues[1].toFloatOrNull()
    }

    private fun checkProgress() {
        if (isCompleted) return

        var maxMeasured = 0.0f

        Tablist.getTablistAsStrings().forEach {
            val chance = extractTrophyChance(it.removeFormatting(), tablistRegex)
            if (chance != null && chance > maxMeasured) {
                maxMeasured = chance
            }
        }

        //~ if >=26.2 'mc.screen' -> 'mc.gui.screen()' {
        val screen = mc.gui.screen()
        //~}
        if (screen != null) {
            val title = screen.title.toUnformattedString()
            val slots = mc.player?.containerMenu?.slots
            if (slots != null) {
                if (title.contains("Your equipment and Stats", ignoreCase = true)) {
                    slots.forEach { slot ->
                        val item = slot.item
                        if (!item.isEmpty) {
                            val itemName = item.hoverName.toUnformattedString()
                            if (itemName.contains("Fishing Stats", ignoreCase = true)) {
                                val lore = item[DataComponents.LORE]
                                if (lore != null) {
                                    lore.lines.forEach { line ->
                                        val chance = extractTrophyChance(line.toUnformattedString(), statRegex)
                                        if (chance != null && chance > maxMeasured) {
                                            maxMeasured = chance
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if (title.contains("Your Stats Breakdown", ignoreCase = true)) {
                    slots.forEach { slot ->
                        val item = slot.item
                        if (!item.isEmpty) {
                            val itemName = item.hoverName.toUnformattedString()
                            val chance = extractTrophyChance(itemName, statRegex)
                            if (chance != null && chance > maxMeasured) {
                                maxMeasured = chance
                            }
                        }
                    }
                }
            }
        }

        if (maxMeasured > highestTrophyChance) {
            highestTrophyChance = maxMeasured
            if (highestTrophyChance >= 150.0f) {
                complete()
            } else {
                AchievementProvider.fireAchievementUpdated(this)
            }
        }
    }

    override fun debugComplete() {
        highestTrophyChance = 150.0f
        super.debugComplete()
    }

    override fun debugReset() {
        highestTrophyChance = 0.0f
        super.debugReset()
    }

    override fun loadState(progressData: Map<String, Any>) {
        super.loadState(progressData)
        highestTrophyChance = (progressData["highestTrophyChance"] as? Number)?.toFloat() ?: 0.0f
    }

    override fun saveState(): Map<String, Any> {
        val state = super.saveState().toMutableMap()
        state["highestTrophyChance"] = highestTrophyChance
        return state
    }
}
