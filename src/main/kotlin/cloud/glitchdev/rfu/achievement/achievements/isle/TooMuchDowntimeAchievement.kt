package cloud.glitchdev.rfu.achievement.achievements.isle

import cloud.glitchdev.rfu.RiccioFishingUtils.mc
import cloud.glitchdev.rfu.achievement.Achievement
import cloud.glitchdev.rfu.achievement.AchievementCategory
import cloud.glitchdev.rfu.achievement.AchievementDifficulty
import cloud.glitchdev.rfu.achievement.AchievementType
import cloud.glitchdev.rfu.achievement.BaseAchievement
import cloud.glitchdev.rfu.events.EventScope
import cloud.glitchdev.rfu.events.managers.MobEvents.registerMobDisposeEvent
import net.minecraft.world.level.levelgen.Heightmap

@Achievement
object TooMuchDowntimeAchievement : BaseAchievement() {
    override val id: String = "too_much_downtime"
    override val name: String = "Too much downtime..."
    override val description: String = "Void a Lord Jawbus."
    override val type: AchievementType = AchievementType.SECRET
    override val difficulty: AchievementDifficulty = AchievementDifficulty.MEDIUM
    override val category: AchievementCategory = AchievementCategory.ISLE

    override fun setupListeners(scope: EventScope) {
        activeListeners.add(registerMobDisposeEvent { entities ->
            val achieved = entities.any { entity ->
                if (entity.sbName != "Lord Jawbus") return@any false

                val world = mc.level ?: return@any false
                val pos = entity.modelEntity.blockPosition()
                val topY = world.getHeight(Heightmap.Types.WORLD_SURFACE_WG, pos.x, pos.z)

                val isFalling = entity.modelEntity.deltaMovement.y < -1
                val isAboveHole = topY <= 0

                isFalling && isAboveHole
            }

            if (achieved) complete()
        })
    }
}