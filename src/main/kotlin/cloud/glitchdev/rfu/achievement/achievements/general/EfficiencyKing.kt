package cloud.glitchdev.rfu.achievement.achievements.general

import cloud.glitchdev.rfu.achievement.Achievement
import cloud.glitchdev.rfu.achievement.AchievementCategory
import cloud.glitchdev.rfu.achievement.AchievementDifficulty
import cloud.glitchdev.rfu.achievement.AchievementType
import cloud.glitchdev.rfu.achievement.types.NumericStageAchievement
import cloud.glitchdev.rfu.events.managers.TickEvents.registerTickEvent
import cloud.glitchdev.rfu.feature.mob.SeaCreatureHour
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

@Achievement
object EfficiencyKing : NumericStageAchievement() {
    override val id: String = "efficiency_king"
    override val name: String = "Efficiency King"
    override val description: String = "Reach 800/900/1000/1100/1200 sc/h and maintain it for 5/10/15/20/30 minutes"
    override val type: AchievementType = AchievementType.NORMAL
    override val difficulty: AchievementDifficulty = AchievementDifficulty.HARD
    override val category: AchievementCategory = AchievementCategory.GENERAL

    override val targetStage: Int = 5

    init {
        addStageInfo(1, "Efficiency Rookie", "Reach 800 sc/h for 5 minutes", AchievementDifficulty.EASY)
        addStageInfo(2, "Efficiency Contender", "Reach 900 sc/h for 10 minutes", AchievementDifficulty.EASY)
        addStageInfo(3, "Efficiency Adept", "Reach 1000 sc/h for 15 minutes", AchievementDifficulty.MEDIUM)
        addStageInfo(4, "Efficiency Veteran", "Reach 1100 sc/h 20 minutes", AchievementDifficulty.HARD)
        addStageInfo(5, "Efficiency King", "Reach 1200 sc/h 30 minutes", AchievementDifficulty.HARD)
    }

    var startTime : Instant = Instant.DISTANT_PAST
    var minSch : Int = Int.MAX_VALUE

    override fun setupListeners() {
        activeListeners.add(registerTickEvent(interval = 20) {
            val sch = SeaCreatureHour.currentScPerHour.toInt()

            if(sch >= getTargetSchForStage(currentStage)) {
                if(startTime == Instant.DISTANT_PAST) {
                    startTime = Clock.System.now()
                    minSch = sch
                    return@registerTickEvent
                }

                if (sch < minSch) {
                    minSch = sch
                }

                val currentDuration = Clock.System.now() - startTime

                if(currentDuration >= getTargetCountForStage(currentStage).minutes) {
                    advanceStage()
                    if (minSch >= getTargetSchForStage(currentStage)) {
                        currentCount = currentDuration.inWholeMinutes.toInt()
                    } else {
                        startTime = Instant.DISTANT_PAST
                        currentCount = 0
                        minSch = Int.MAX_VALUE
                    }
                } else {
                    currentCount = currentDuration.inWholeMinutes.toInt()
                }

            } else {
                startTime = Instant.DISTANT_PAST
                currentCount = 0
                minSch = Int.MAX_VALUE
            }
        })
    }

    private fun getTargetSchForStage(stage: Int): Int {
        return when(stage) {
            1 -> 800
            2 -> 900
            3 -> 1000
            4 -> 1100
            5 -> 1200
            else -> 1200
        }
    }

    override fun getTargetCountForStage(stage: Int): Int {
        return when(stage) {
            1 -> 5
            2 -> 10
            3 -> 15
            4 -> 20
            5 -> 30
            else -> 30
        }
    }
}