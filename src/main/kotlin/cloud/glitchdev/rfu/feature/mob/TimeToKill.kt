package cloud.glitchdev.rfu.feature.mob

import cloud.glitchdev.rfu.RiccioFishingUtils.mc
import cloud.glitchdev.rfu.config.categories.SeaCreatureConfig
import cloud.glitchdev.rfu.constants.fishing.SeaCreatures
import cloud.glitchdev.rfu.constants.text.TextColor
import cloud.glitchdev.rfu.data.catches.CatchTracker
import cloud.glitchdev.rfu.data.mob.SkyblockEntity
import cloud.glitchdev.rfu.events.managers.MobEvents.registerMobDeathEvent
import cloud.glitchdev.rfu.events.managers.MobEvents.registerMobDisposeEvent
import cloud.glitchdev.rfu.feature.Feature
import cloud.glitchdev.rfu.feature.RFUFeature
import cloud.glitchdev.rfu.utils.Chat
import cloud.glitchdev.rfu.utils.TextUtils
import cloud.glitchdev.rfu.utils.dsl.toReadableString
import kotlin.time.Clock

@RFUFeature
object TimeToKill : Feature {
    private val encouragements = listOf("Amazing", "Incredible", "Outstanding", "Fantastic", "Brilliant", "Unreal")

    override fun onInitialize() {
        registerMobDisposeEvent { entities ->
            onMobDeath(entities, true)
        }

        registerMobDeathEvent { entities ->
            onMobDeath(entities)
        }
    }

    private fun onMobDeath(entities: Set<SkyblockEntity>, avoidDead: Boolean = false) {
        entities.forEach { entity ->
            val sc = SeaCreatures.get(entity.sbName) ?: return@forEach
            if (avoidDead && entity.isDying) return@forEach
            val player = mc.player ?: return@forEach
            if (player.distanceTo(entity.modelEntity) > 40f) return@forEach
            val duration = Clock.System.now() - entity.createdAt

            if (sc.special && SeaCreatureConfig.timeToKill) {
                Chat.sendMessage(
                    TextUtils.rfuLiteral("${TextColor.YELLOW}${entity.sbName} ${TextColor.GOLD}took ${TextColor.YELLOW}${duration.toReadableString(true)} ${TextColor.GOLD}to kill!")
                )
            }

            if (CatchTracker.catchHistory.registerKillTime(sc, duration.inWholeMilliseconds)) {
                CatchTracker.catchesFile.save()
                if (sc.special && SeaCreatureConfig.killTimePersonalBest) {
                    Chat.sendMessage(
                        TextUtils.rfuLiteral("${TextColor.YELLOW}${encouragements.random()}! ${TextColor.GOLD}New ${TextColor.YELLOW}${entity.sbName} ${TextColor.GOLD}Personal Best! ${TextColor.YELLOW}${duration.toReadableString(true)}")
                    )
                }
            }
        }
    }
}