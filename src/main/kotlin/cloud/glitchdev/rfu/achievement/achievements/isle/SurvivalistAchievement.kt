package cloud.glitchdev.rfu.achievement.achievements.isle

import cloud.glitchdev.rfu.RiccioFishingUtils.mc
import cloud.glitchdev.rfu.achievement.*
import cloud.glitchdev.rfu.constants.SeaCreatures
import cloud.glitchdev.rfu.data.mob.SkyblockEntity
import cloud.glitchdev.rfu.events.managers.ChatEvents.registerGameEvent
import cloud.glitchdev.rfu.events.managers.MobEvents.registerMobDetectEvent
import cloud.glitchdev.rfu.events.managers.MobEvents.registerMobDisposeEvent
import cloud.glitchdev.rfu.events.managers.PlayerEvents.registerPlayerDetectEvent
import cloud.glitchdev.rfu.events.managers.SeaCreatureCatchEvents.registerSeaCreatureCatchEvent
import net.minecraft.world.level.levelgen.Heightmap

@Achievement
object SurvivalistAchievement : BaseAchievement() {
    override val id: String = "survivalist"
    override val name: String = "Survivalist"
    override val description: String = "Solo your own jawbus without dying.\nNo player must be nearby for this."
    override val type: AchievementType = AchievementType.NORMAL
    override val difficulty: AchievementDifficulty = AchievementDifficulty.HARD
    override val category: AchievementCategory = AchievementCategory.ISLE

    private var trackingJawbus: SkyblockEntity? = null
    private var waitingForJawbus = false
    private var wasAssisted = false

    private val DEATH_REGEX = """ ☠ You """.toRegex()
    private val SEA_CREATURE = SeaCreatures.MAGMA_SLUG

    override fun setupListeners() {
        activeListeners.add(registerSeaCreatureCatchEvent { sc, _ ->
            if (sc == SEA_CREATURE) {
                waitingForJawbus = true
                wasAssisted = false
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
                if (!wasAssisted) {
                    val world = mc.level
                    val pos = jawbus.modelEntity.blockPosition()
                    val topY = world?.getHeight(Heightmap.Types.WORLD_SURFACE_WG, pos.x, pos.z) ?: 255

                    val isFalling = jawbus.modelEntity.deltaMovement.y < -1
                    val isAboveHole = topY <= 0
                    val isVoided = isFalling && isAboveHole

                    if (!isVoided) {
                        complete()
                    }
                }
                trackingJawbus = null
            }
        })
    }
}
