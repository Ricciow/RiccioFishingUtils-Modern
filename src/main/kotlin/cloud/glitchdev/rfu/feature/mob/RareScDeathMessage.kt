package cloud.glitchdev.rfu.feature.mob

import cloud.glitchdev.rfu.RiccioFishingUtils.mc
import cloud.glitchdev.rfu.config.categories.SeaCreatureConfig
import cloud.glitchdev.rfu.constants.fishing.BobbinTimeBonus
import cloud.glitchdev.rfu.constants.fishing.SeaCreatures
import cloud.glitchdev.rfu.constants.text.TextColor
import cloud.glitchdev.rfu.data.catches.CatchTracker
import cloud.glitchdev.rfu.data.mob.SkyblockEntity
import cloud.glitchdev.rfu.events.managers.MobEvents.registerMobDeathEvent
import cloud.glitchdev.rfu.events.managers.MobEvents.registerMobDisposeEvent
import cloud.glitchdev.rfu.events.managers.SeaCreaturePersonalBestEvents
import cloud.glitchdev.rfu.feature.Feature
import cloud.glitchdev.rfu.feature.RFUFeature
import cloud.glitchdev.rfu.feature.fishing.BobbinTime
import cloud.glitchdev.rfu.utils.Chat
import cloud.glitchdev.rfu.utils.TextUtils
import cloud.glitchdev.rfu.utils.dsl.toReadableString
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.HoverEvent
import net.minecraft.network.chat.Style
import kotlin.time.Clock

@RFUFeature
object RareScDeathMessage : Feature {
    private val encouragements = listOf("Amazing", "Incredible", "Outstanding", "Fantastic", "Brilliant", "Unreal")

    override fun onInitialize() {
        registerMobDisposeEvent { entities ->
            onMobDeath(entities, isDisposal = true)
        }

        registerMobDeathEvent { entities ->
            onMobDeath(entities)
        }
    }

    private fun onMobDeath(entities: Set<SkyblockEntity>, isDisposal: Boolean = false) {
        entities.forEach { entity ->
            val sc = SeaCreatures.get(entity.sbName) ?: return@forEach
            if (isDisposal && entity.isDying) return@forEach
            val player = mc.player ?: return@forEach
            if (player.distanceTo(entity.modelEntity) > 40f) return@forEach
            val duration = Clock.System.now() - entity.createdAt
            val isPersonalBest = !isDisposal && entity.isOwn() &&
                CatchTracker.catchHistory.registerKillTime(sc, duration.inWholeMilliseconds)

            if (isPersonalBest) {
                CatchTracker.catchesFile.save()
                SeaCreaturePersonalBestEvents.runTasks(sc, duration.inWholeMilliseconds)
            }
            if (!sc.special) return@forEach

            val showPersonalBest = isPersonalBest && SeaCreatureConfig.killTimePersonalBest
            val bobbinBonus = if (!isDisposal && SeaCreatureConfig.bobbinTimeDeathMessage) {
                BobbinTime.calculateCurrentBuffPercentage(player)
            } else {
                null
            }

            val text = when {
                showPersonalBest -> "${TextColor.YELLOW}${encouragements.random()}! ${TextColor.GOLD}New ${TextColor.YELLOW}${entity.sbName} ${TextColor.GOLD}Personal Best! ${TextColor.YELLOW}${duration.toReadableString(true)}"
                SeaCreatureConfig.timeToKill -> "${TextColor.YELLOW}${entity.sbName} ${TextColor.GOLD}took ${TextColor.YELLOW}${duration.toReadableString(true)} ${TextColor.GOLD}to kill!"
                bobbinBonus != null -> "${TextColor.YELLOW}${entity.sbName} ${TextColor.GOLD}died!"
                else -> return@forEach
            }

            val message = TextUtils.rfuLiteral(text)
            if (bobbinBonus != null) {
                val amount = BobbinTimeBonus.formatAmount(bobbinBonus)
                message.append(
                    Component.literal(" ${TextColor.AQUAMARINE}(+$amount${BobbinTimeBonus.MAGIC_FIND.suffix})")
                        .withStyle(Style.EMPTY.withHoverEvent(HoverEvent.ShowText(Component.literal("${TextColor.DARK_GRAY}Bobbin' Time Bonus"))))
                )
            }

            Chat.sendMessage(message)
        }
    }
}
