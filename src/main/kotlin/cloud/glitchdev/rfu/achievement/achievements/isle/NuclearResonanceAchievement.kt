package cloud.glitchdev.rfu.achievement.achievements.isle

import cloud.glitchdev.rfu.RiccioFishingUtils.mc
import cloud.glitchdev.rfu.achievement.Achievement
import cloud.glitchdev.rfu.achievement.AchievementCategory
import cloud.glitchdev.rfu.achievement.AchievementDifficulty
import cloud.glitchdev.rfu.achievement.AchievementType
import cloud.glitchdev.rfu.achievement.types.NumericAchievement
import cloud.glitchdev.rfu.constants.RareDrops
import cloud.glitchdev.rfu.constants.SeaCreatures
import cloud.glitchdev.rfu.data.mob.SkyblockEntity
import cloud.glitchdev.rfu.events.managers.DropEvents.registerRareDropEvent
import cloud.glitchdev.rfu.events.managers.MobEvents.registerMobDetectEvent
import cloud.glitchdev.rfu.events.managers.MobEvents.registerMobDisposeEvent
import cloud.glitchdev.rfu.events.managers.TickEvents.registerTickEvent
import kotlin.math.abs

@Achievement
object NuclearResonanceAchievement : NumericAchievement() {
    override val id: String = "nuclear_resonance"
    override val name: String = "Nuclear Resonance"
    override val description: String = "Drop 2 Radioactive Vials back-to-back (Counts if you're close to a jawbus)"
    override val type: AchievementType = AchievementType.NORMAL
    override val difficulty: AchievementDifficulty = AchievementDifficulty.IMPOSSIBLE
    override val category: AchievementCategory = AchievementCategory.ISLE
    override val targetCount: Long = 2L

    private val SEA_CREATURE = SeaCreatures.get("Lord Jawbus")!!
    private val DROP = RareDrops.RADIOACTIVE_VIAL
    private val foundJawbusses: MutableSet<SkyblockEntity> = mutableSetOf()

    private val unmatchedDisposals = mutableListOf<Long>()
    private val unmatchedDrops = mutableListOf<Long>()

    override fun setupListeners() {
        activeListeners.add(registerMobDetectEvent { entities ->
            entities.forEach { entity ->
                if (entity.sbName == SEA_CREATURE.scName) {
                    foundJawbusses.add(entity)
                }
            }
        })

        activeListeners.add(registerMobDisposeEvent { entities ->
            entities.forEach { entity ->
                if (foundJawbusses.remove(entity) &&
                    (mc.player?.distanceTo(entity.modelEntity) ?: Float.MAX_VALUE) <= 45.0
                ) {
                    unmatchedDisposals.add(System.currentTimeMillis())
                }
            }
        })

        activeListeners.add(registerRareDropEvent { rareDrop, _ ->
            if (rareDrop == DROP) {
                unmatchedDrops.add(System.currentTimeMillis())
            }
            true
        })

        activeListeners.add(registerTickEvent(interval = 20) {
            val now = System.currentTimeMillis()

            val disposalIterator = unmatchedDisposals.iterator()
            while (disposalIterator.hasNext()) {
                val disposalTime = disposalIterator.next()

                val matchingDropIndex = unmatchedDrops.indexOfFirst { dropTime ->
                    abs(disposalTime - dropTime) <= 4000
                }

                if (matchingDropIndex != -1) {
                    disposalIterator.remove()
                    unmatchedDrops.removeAt(matchingDropIndex)
                    addProgress(1L)
                }
            }

            if (unmatchedDisposals.any { now - it > 4000 }) {
                unmatchedDisposals.clear()
                unmatchedDrops.clear()
                currentCount = 0L
            }

            unmatchedDrops.removeAll { now - it > 4000 }
        })
    }
}