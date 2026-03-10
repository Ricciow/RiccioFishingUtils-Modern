package cloud.glitchdev.rfu.achievement.achievements

import cloud.glitchdev.rfu.achievement.Achievement
import cloud.glitchdev.rfu.achievement.AchievementCategory
import cloud.glitchdev.rfu.achievement.AchievementDifficulty
import cloud.glitchdev.rfu.achievement.AchievementType
import cloud.glitchdev.rfu.achievement.types.StageAchievement
import cloud.glitchdev.rfu.events.managers.SeaCreatureCatchEvents.registerSeaCreatureCatchEvent

@Achievement
object LuckyHookAchievement : StageAchievement() {
    override val targetStage: Int = 10

    init {
        addStageInfo(1, "Lucky Hook: Magma Slug", "Catch a Magma Slug b2b", AchievementDifficulty.EASY)
        addStageInfo(2, "Lucky Hook: Moogma", "Catch a Moogma b2b", AchievementDifficulty.EASY)
        addStageInfo(3, "Lucky Hook: Lava Leech", "Catch a Lava Leech b2b", AchievementDifficulty.EASY)
        addStageInfo(4, "Lucky Hook: Pyroclastic Worm", "Catch a Pyroclastic Worm b2b", AchievementDifficulty.MEDIUM)
        addStageInfo(5, "Lucky Hook: Lava Flame", "Catch a Lava Flame b2b", AchievementDifficulty.MEDIUM)
        addStageInfo(6, "Lucky Hook: Fire Eel", "Catch a Fire Eel b2b", AchievementDifficulty.MEDIUM)
        addStageInfo(7, "Lucky Hook: Taurus", "Catch a Taurus b2b", AchievementDifficulty.HARD)
        addStageInfo(8, "Lucky Hook: Thunder", "Catch a Thunder b2b", AchievementDifficulty.HARD)
        addStageInfo(9, "Lucky Hook: Lord Jawbus", "Catch a Lord Jawbus b2b", AchievementDifficulty.VERY_HARD)
        addStageInfo(10, "Lucky Hook: Plhlegblast", "Catch a Plhlegblast b2b", AchievementDifficulty.IMPOSSIBLE)
    }

    var lastSC : String = ""

    override var currentStage: Int = 1
        set(value) {
            field = value

            if (field > targetStage) {
                complete()
            }
        }

    override val currentProgress: Int
        get() = if(getCurrentSc() == lastSC) 1 else 0
    override val targetProgress: Int = 2

    override fun setupListeners() {
        activeListeners.add(registerSeaCreatureCatchEvent { sc, _ ->
            val lookingFor = getCurrentSc() ?: return@registerSeaCreatureCatchEvent

            if(lookingFor == sc.scName) {
                _progress = 0.5f

                if(lookingFor == lastSC) {
                    advanceStage()
                    _progress = 0f
                }
            } else {
                _progress = 0f
            }

            lastSC = sc.scName
        })
    }

    private fun getCurrentSc() : String? {
        return getStageName(currentStage)?.substringAfter("Lucky Hook: ")
    }

    override val id: String = "lucky_hook"
    override val name: String = "Lucky Hook"
    override val description: String = "Catch one of each non-hotspot lava sea creature back to back"
    override val type: AchievementType = AchievementType.NORMAL
    override val difficulty: AchievementDifficulty = AchievementDifficulty.HARD
    override val category: AchievementCategory = AchievementCategory.ISLE
}