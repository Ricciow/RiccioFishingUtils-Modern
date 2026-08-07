package cloud.glitchdev.rfu.achievement.achievements.isle

import cloud.glitchdev.rfu.RiccioFishingUtils.mc
import cloud.glitchdev.rfu.achievement.*
import cloud.glitchdev.rfu.achievement.types.StageAchievement
import cloud.glitchdev.rfu.config.categories.LavaFishing
import cloud.glitchdev.rfu.constants.fishing.SeaCreatures
import cloud.glitchdev.rfu.data.mob.SkyblockEntity
import cloud.glitchdev.rfu.events.managers.ChatEvents.registerGameEvent
import cloud.glitchdev.rfu.events.managers.MobEvents.registerMobDetectEvent
import cloud.glitchdev.rfu.events.managers.MobEvents.registerMobDisposeEvent
import cloud.glitchdev.rfu.events.managers.PlayerEvents.registerPlayerDetectEvent
import cloud.glitchdev.rfu.events.managers.SeaCreatureCatchEvents.registerSeaCreatureCatchEvent
import cloud.glitchdev.rfu.events.managers.TickEvents.registerTickEvent
import cloud.glitchdev.rfu.utils.dsl.parseHealthValue
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.level.levelgen.Heightmap

@Achievement
object SurvivalistAchievement : StageAchievement() {
    override val id: String = "survivalist"
    override val name: String = "Survivalist"
    override val description: String = "Prove your mastery over Jawbus through increasingly deadly challenges."
    override val type: AchievementType = AchievementType.NORMAL
    override val difficulty: AchievementDifficulty = AchievementDifficulty.IMPOSSIBLE
    override val category: AchievementCategory = AchievementCategory.ISLE
    override val targetStage: Int = 8

    private var trackingJawbus: SkyblockEntity? = null
    private var waitingForJawbus = false
    private var wasAssisted = false
    private var wasDoubleHook = false
    private var hadHardMode = false
    private var disqualifiedForArmor = false
    private var maxJawbusHealth = -1
    private var jawbusSpawnTime = 0L

    private val armorSlots = arrayOf(EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET)
    private val DEATH_REGEX = """ ☠ You """.toRegex()
    private val SEA_CREATURE = SeaCreatures.get("Lord Jawbus")!!

    init {
        addStageInfo(1, "Rookie Survivalist", "Solo your own Jawbus without dying.\nNo player must be nearby for this.", AchievementDifficulty.EASY)
        addStageInfo(2, "Rookie Survivalist+", "Solo kill a Jawbus you double hooked.\nNo player must be nearby for this.", AchievementDifficulty.MEDIUM)
        addStageInfo(3, "Intermediate Survivalist", "Solo kill a Jawbus with Hard Mode enabled.\nNo player must be nearby for this.", AchievementDifficulty.MEDIUM)
        addStageInfo(4, "Intermediate Survivalist+", "Solo kill a double hooked Jawbus with Hard Mode enabled.\nNo player must be nearby for this.", AchievementDifficulty.HARD)
        addStageInfo(5, "Experienced Survivalist", "Solo kill a Jawbus without armor.\nNo player must be nearby for this.", AchievementDifficulty.VERY_HARD)
        addStageInfo(6, "Experienced Survivalist+", "Solo kill a double hooked Jawbus without armor.\nNo player must be nearby for this.", AchievementDifficulty.VERY_HARD)
        addStageInfo(7, "Professional Survivalist", "Solo kill a Jawbus without armor with Hard Mode enabled.\nNo player must be nearby for this.", AchievementDifficulty.IMPOSSIBLE)
        addStageInfo(8, "Professional Survivalist+", "Solo kill a double hooked Jawbus without armor with Hard Mode enabled.\nNo player must be nearby for this.", AchievementDifficulty.IMPOSSIBLE)
    }

    override fun setupListeners() {
        activeListeners.add(registerSeaCreatureCatchEvent { sc, doubleHook, _, _, _ ->
            if (sc == SEA_CREATURE) {
                waitingForJawbus = true
                wasAssisted = false
                wasDoubleHook = doubleHook
                hadHardMode = LavaFishing.jawbus_hard_mode
                disqualifiedForArmor = false
                maxJawbusHealth = -1
                jawbusSpawnTime = System.currentTimeMillis()
                trackingJawbus = null
            }
        })

        activeListeners.add(registerTickEvent(interval = 5) {
            val jawbus = trackingJawbus
            if (jawbus != null) {
                val currentHealth = jawbus.health.parseHealthValue()
                if (currentHealth > 0) {
                    if (maxJawbusHealth == -1 || currentHealth > maxJawbusHealth) {
                        maxJawbusHealth = currentHealth
                    }

                    val player = mc.player
                    val isWearingArmor = player != null && armorSlots.any { slot -> !player.getItemBySlot(slot).isEmpty }

                    if (isWearingArmor && maxJawbusHealth > 0) {
                        val damageDealt = maxJawbusHealth - currentHealth
                        val timeElapsed = System.currentTimeMillis() - jawbusSpawnTime
                        val isGracePeriodActive = jawbusSpawnTime > 0L && timeElapsed <= 3000L

                        if (isGracePeriodActive) {
                            if (damageDealt > 5_000_000) {
                                disqualifiedForArmor = true
                            }
                        } else {
                            if (damageDealt > 0) {
                                disqualifiedForArmor = true
                            }
                        }
                    }
                }
            }
        })

        activeListeners.add(registerMobDetectEvent { entities ->
            if (waitingForJawbus) {
                val jawbus = entities.find { it.sbName == SEA_CREATURE.scName }
                if (jawbus != null) {
                    trackingJawbus = jawbus
                    waitingForJawbus = false
                    if (jawbusSpawnTime == 0L) {
                        jawbusSpawnTime = System.currentTimeMillis()
                    }
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
                    val isValidArmorless = !disqualifiedForArmor
                    when (currentStage) {
                        1 -> if (!wasAssisted) advanceStage()
                        2 -> if (!wasAssisted && wasDoubleHook) advanceStage()
                        3 -> if (!wasAssisted && hadHardMode) advanceStage()
                        4 -> if (!wasAssisted && wasDoubleHook && hadHardMode) advanceStage()
                        5 -> if (!wasAssisted && isValidArmorless) advanceStage()
                        6 -> if (!wasAssisted && isValidArmorless && wasDoubleHook) advanceStage()
                        7 -> if (!wasAssisted && isValidArmorless && hadHardMode) advanceStage()
                        8 -> if (!wasAssisted && isValidArmorless && wasDoubleHook && hadHardMode) complete()
                    }
                }

                trackingJawbus = null
            }
        })
    }
}
