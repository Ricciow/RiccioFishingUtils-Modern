package cloud.glitchdev.rfu.feature.mob

import cloud.glitchdev.rfu.config.categories.SeaCreatureConfig
import cloud.glitchdev.rfu.config.categories.SeaCreatureConfig.RARE_SC_REGEX
import cloud.glitchdev.rfu.constants.text.TextColor
import cloud.glitchdev.rfu.events.managers.MobEvents.registerMobDisposeEvent
import cloud.glitchdev.rfu.feature.Feature
import cloud.glitchdev.rfu.feature.RFUFeature
import cloud.glitchdev.rfu.utils.Chat
import cloud.glitchdev.rfu.utils.TextUtils
import cloud.glitchdev.rfu.utils.dsl.toReadableString
import kotlin.time.Clock

@RFUFeature
object TimeToKill : Feature {
    override fun onInitialize() {
        registerMobDisposeEvent { entities ->
            if(!SeaCreatureConfig.timeToKill) return@registerMobDisposeEvent
            val entities = entities.filter { RARE_SC_REGEX.matches(it.sbName) }
            entities.forEach { entity ->
                val duration = Clock.System.now() - entity.createdAt

                Chat.sendMessage(
                    TextUtils.rfuLiteral("${TextColor.YELLOW}${entity.sbName} ${TextColor.GOLD}took ${TextColor.YELLOW}${duration.toReadableString(true)} ${TextColor.GOLD}to kill!")
                )
            }
        }
    }
}