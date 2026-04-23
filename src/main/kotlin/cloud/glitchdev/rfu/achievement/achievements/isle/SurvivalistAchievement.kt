package cloud.glitchdev.rfu.achievement.achievements.isle

import cloud.glitchdev.rfu.RiccioFishingUtils.mc
import cloud.glitchdev.rfu.achievement.*
import cloud.glitchdev.rfu.achievement.types.StageAchievement
import cloud.glitchdev.rfu.config.categories.LavaFishing
import cloud.glitchdev.rfu.constants.SeaCreatures
import cloud.glitchdev.rfu.data.mob.SkyblockEntity
import cloud.glitchdev.rfu.events.managers.ChatEvents.registerGameEvent
import cloud.glitchdev.rfu.events.managers.MobEvents.registerMobDetectEvent
import cloud.glitchdev.rfu.events.managers.MobEvents.registerMobDisposeEvent
import cloud.glitchdev.rfu.events.managers.PlayerEvents.registerPlayerDetectEvent
import cloud.glitchdev.rfu.events.managers.SeaCreatureCatchEvents.registerSeaCreatureCatchEvent
import net.minecraft.world.level.levelgen.Heightmap

@Achievement
object SurvivalistAchievement : StageAchievement() {
    override val id: String = "survivalist"
    override val name: String = "Survivalist"
    override val description: String = "Prove your mastery over Jawbus through increasingly deadly challenges."
    override val type: AchievementType = AchievementType.NORMAL
    override val difficulty: AchievementDifficulty = AchievementDifficulty.VERY_HARD
    override val category: AchievementCategory = AchievementCategory.ISLE
    override val targetStage: Int = 4

    private var trackingJawbus: SkyblockEntity? = null
    private var waitingForJawbus = false
    private var wasAssisted = false
    private var wasDoubleHook = false
    private var hadHardMode = false

    private val DEATH_REGEX = """ ☠ You """.toRegex()
    private val SEA_CREATURE = SeaCreatures.get("Lord Jawbus")!!

    init {
        addStageInfo(1, "Survivalist-", "Solo your own Jawbus without dying.\nNo player must be nearby for this.", AchievementDifficulty.HARD)
        addStageInfo(2, "Survivalist", "Solo kill a Jawbus you double hooked.\nNo player must be nearby for this.", AchievementDifficulty.HARD)
        addStageInfo(3, "Survivalist+", "Solo kill a Jawbus with Hard Mode enabled.\nNo player must be nearby for this.", AchievementDifficulty.VERY_HARD)
        addStageInfo(4, "Survivalist++", "Solo kill a double hooked Jawbus with Hard Mode enabled.\nNo player must be nearby for this.", AchievementDifficulty.VERY_HARD)
    }

    override fun setupListeners() {
        activeListeners.add(registerSeaCreatureCatchEvent { sc, doubleHook, _, _, _ ->
            if (sc == SEA_CREATURE) {
                waitingForJawbus = true
                wasAssisted = false
                wasDoubleHook = doubleHook
                hadHardMode = LavaFishing.jawbus_hard_mode
                trackingJawbus = null
            }
        })

        activeListeners.add(registerMobDetectEvent { entities ->
            if (waitingForJawbus) {
                val jawbus = entities.find { it.sbName == SEA_CREATURE.scName }
                if (jawbus != null) {
                    trackingJawbus = jawbus
                    waitingForJawbus = false
                }
            }
        })

        activeListeners.add(registerPlayerDetectEvent { players ->
            val jawbus = trackingJawbus ?: return@registerPlayerDetectEvent
            if (wasAssisted) return@registerPlayerDetectEvent

            val assisted = players.any { player ->
                player.distanceTo(jawbus.modelEntity) < 25.0
            }

            if (assisted) {
                wasAssisted = true
            }
        })

        activeListeners.add(registerGameEvent(DEATH_REGEX) { _, _, _ ->
            trackingJawbus = null
            waitingForJawbus = false
        })

        activeListeners.add(registerMobDisposeEvent { entities ->
            val jawbus = trackingJawbus ?: return@registerMobDisposeEvent
            if (entities.contains(jawbus)) {
                val world = mc.level
                val pos = jawbus.modelEntity.blockPosition()
                val topY = world?.getHeight(Heightmap.Types.WORLD_SURFACE_WG, pos.x, pos.z) ?: 255

                val isFalling = jawbus.modelEntity.deltaMovement.y < -1
                val isAboveHole = topY <= 0
                val isVoided = isFalling && isAboveHole

                if (!isVoided) {
                    when (currentStage) {
                        1 -> if (!wasAssisted) advanceStage()
                        2 -> if (!wasAssisted && wasDoubleHook) advanceStage()
                        3 -> if (!wasAssisted && hadHardMode) advanceStage()
                        4 -> if (!wasAssisted && wasDoubleHook && hadHardMode) complete()
                    }
                }

                trackingJawbus = null
            }
        })
    }
}
