package cloud.glitchdev.rfu.achievement.achievements.general

import cloud.glitchdev.rfu.achievement.Achievement
import cloud.glitchdev.rfu.achievement.AchievementCategory
import cloud.glitchdev.rfu.achievement.AchievementDifficulty
import cloud.glitchdev.rfu.achievement.AchievementType
import cloud.glitchdev.rfu.achievement.types.NumericStageAchievement
import cloud.glitchdev.rfu.constants.Skills
import cloud.glitchdev.rfu.events.managers.ChatEvents.registerGameEvent
import cloud.glitchdev.rfu.events.managers.ContainerEvents.registerContainerOpenEvent
import gg.essential.universal.utils.toUnformattedString
import net.minecraft.core.component.DataComponents

@Achievement
object FishingGodAchievement : NumericStageAchievement() {
    override val id = "fishing_god"
    override val name = "Fishing God"
    override val description = "Accumulate massive amounts of fishing experience."
    override val type = AchievementType.NORMAL
    override val difficulty = AchievementDifficulty.IMPOSSIBLE
    override val category = AchievementCategory.GENERAL

    override val targetStage = 15
    override val resetCountOnStageAdvance = false

    private val MILESTONES = listOf(
        25_000_000L, 50_000_000L, 100_000_000L, 250_000_000L, 500_000_000L,
        750_000_000L, 1_000_000_000L, 1_500_000_000L, 2_000_000_000L, 2_500_000_000L,
        3_000_000_000L, 3_500_000_000L, 4_000_000_000L, 4_500_000_000L, 5_000_000_000L
    )

    private val MILESTONE_NAMES = listOf(
        "Novice Angler", "Skilled Fisherman", "Master Caster", "Elite Reeler", "Oceanic Explorer",
        "Abyssal Champion", "Legendary Harpooner", "Tidal King", "Poseidon's Enemy", "World Class Fisher",
        "Mythical Mariner", "Cosmic Angler", "Eternal Fisher", "Omnipotent Angler", "Fishing God"
    )

    init {
        MILESTONES.forEachIndexed { index, milestone ->
            val stage = index + 1
            val formatted = formatXp(milestone)
            
            val stageDifficulty = when {
                milestone >= 1_000_000_000L -> AchievementDifficulty.IMPOSSIBLE
                milestone >= 750_000_000L -> AchievementDifficulty.VERY_HARD
                milestone >= 500_000_000L -> AchievementDifficulty.HARD
                milestone >= 250_000_000L -> AchievementDifficulty.MEDIUM
                else -> AchievementDifficulty.EASY
            }

            addStageInfo(stage, MILESTONE_NAMES[index], "Reach $formatted Fishing XP", stageDifficulty)
        }
    }

    private fun formatXp(xp: Long): String {
        return when {
            xp >= 1_000_000_000L -> "${xp / 1_000_000_000.0}B"
            xp >= 1_000_000L -> "${xp / 1_000_000.0}M"
            xp >= 1_000L -> "${xp / 1_000.0}k"
            else -> xp.toString()
        }.replace(".0", "")
    }

    override fun getTargetCountForStage(stage: Int): Long {
        return MILESTONES.getOrNull(stage - 1) ?: MILESTONES.last()
    }

    var totalFishingXp: Long = 0
        private set(value) {
            field = value
            currentCount = value
            while (!isCompleted && currentCount >= targetCount) {
                advanceStage()
            }
        }

    private val OVERLAY_REGEX = """\+([0-9,]+(?:\.[0-9]+)?) Fishing \(([^/]+)/([^)]+)\)""".toRegex()
    private val LORE_LEVEL_REGEX = """Progress to Level (\d+)""".toRegex()
    private val LORE_PROGRESS_REGEX = """([0-9,]+[kMB]?)/([0-9,]+[kMB]?)""".toRegex()
    private val LORE_TOTAL_XP_REGEX = """([0-9,]{5,}(?:\.[0-9]+)?)""".toRegex()

    override fun setupListeners() {
        activeListeners.add(registerGameEvent(OVERLAY_REGEX, isOverlay = true) { _, _, matches ->
            val currentXpStr = matches?.groupValues?.getOrNull(2) ?: return@registerGameEvent
            val requiredXpStr = matches.groupValues.getOrNull(3) ?: return@registerGameEvent

            val x = parseXp(currentXpStr)
            val y = parseXp(requiredXpStr)

            val calculatedTotalXp = if (y == 0L) {
                Skills.getTotalXpAtLevel(50) + x
            } else {
                val index = Skills.XP_REQUIRED_FOR_LEVEL.indexOf(y)
                if (index != -1) {
                    Skills.getTotalXpAtLevel(index) + x
                } else {
                    0L
                }
            }

            if (calculatedTotalXp > totalFishingXp) {
                totalFishingXp = calculatedTotalXp
            }
        })

        activeListeners.add(registerContainerOpenEvent { _, items ->
            for (item in items) {
                val loreLines = item[DataComponents.LORE]?.lines?.map { it.toUnformattedString() } ?: continue
                val loreText = loreLines.joinToString(" ")

                if (!loreText.contains("Visit your local pond to fish and earn Fishing XP!")) continue
                println(loreText)

                var parsedTotalXp = 0L
                
                if (loreText.contains("Max Skill level reached!")) {
                    val match = LORE_TOTAL_XP_REGEX.findAll(loreText).lastOrNull()
                    if (match != null) {
                        parsedTotalXp = parseXp(match.groupValues[1])
                    }
                } else {
                    val levelMatch = LORE_LEVEL_REGEX.find(loreText)
                    val progressMatch = LORE_PROGRESS_REGEX.find(loreText)
                    
                    if (levelMatch != null && progressMatch != null) {
                        val nextLevel = levelMatch.groupValues[1].toInt()
                        val currentLevel = nextLevel - 1
                        val currentXp = parseXp(progressMatch.groupValues[1])
                        parsedTotalXp = Skills.getTotalXpAtLevel(currentLevel) + currentXp
                    }
                }
                
                if (parsedTotalXp > totalFishingXp) {
                    totalFishingXp = parsedTotalXp
                }
            }
        })
    }

    private fun parseXp(str: String): Long {
        var s = str.replace(",", "").trim()
        val multiplier = when {
            s.endsWith("k", ignoreCase = true) -> {
                s = s.dropLast(1)
                1_000L
            }
            s.endsWith("M", ignoreCase = true) -> {
                s = s.dropLast(1)
                1_000_000L
            }
            s.endsWith("B", ignoreCase = true) -> {
                s = s.dropLast(1)
                1_000_000_000L
            }
            else -> 1L
        }
        return (s.toDoubleOrNull()?.let { it * multiplier } ?: 0.0).toLong()
    }

    override fun saveState(): Map<String, Any> {
        val state = super.saveState().toMutableMap()
        state["totalFishingXp"] = totalFishingXp
        return state
    }

    override fun loadState(progressData: Map<String, Any>) {
        super.loadState(progressData)
        totalFishingXp = (progressData["totalFishingXp"] as? Number)?.toLong() ?: 0L
    }
}
