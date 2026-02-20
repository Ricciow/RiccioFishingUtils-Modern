package cloud.glitchdev.rfu.feature.drops

import cloud.glitchdev.rfu.config.categories.GeneralFishing
import cloud.glitchdev.rfu.constants.RareDrops
import cloud.glitchdev.rfu.events.managers.DropEvents
import cloud.glitchdev.rfu.feature.Feature
import cloud.glitchdev.rfu.feature.RFUFeature
import cloud.glitchdev.rfu.manager.drops.DropManager
import cloud.glitchdev.rfu.utils.Chat.sendMessage
import cloud.glitchdev.rfu.utils.Chat.sendPartyMessage
import cloud.glitchdev.rfu.utils.dsl.toReadableString
import gg.essential.universal.utils.toUnformattedString
import net.minecraft.network.chat.Component

@RFUFeature
object CustomRareDropMessage : Feature {
    override fun onInitialize() {
        DropEvents.registerRareDropEvent { rareDrop, magicFind ->
            println("Rare drop")
            sendCustomMessage(rareDrop, magicFind)
        }
    }

    private fun sendCustomMessage(rareDrop: RareDrops, magicFind: Int?) {
        if (!GeneralFishing.customRareDropMessage) return

        val entry = DropManager.dropHistory.getOrAdd(rareDrop)
        val currentDrop = entry.history.lastOrNull() ?: return
        val previousDropIndex = entry.history.lastIndex - 1
        val previousDrop = if (previousDropIndex >= 0) entry.history[previousDropIndex] else null

        val timeSinceLast = if (previousDrop != null) {
            (currentDrop.date - previousDrop.date).toReadableString()
        } else {
            "First Drop"
        }

        val messageString = GeneralFishing.rareDropMessageFormat
            .replace("{drop}", rareDrop.dropName)
            .replace("{magic_find}", magicFind?.toString() ?: "0")
            .replace("{count}", currentDrop.sinceCount.toString())
            .replace("{time}", timeSinceLast)
            .replace("&", "§")

        val message = Component.literal(messageString)

        sendMessage(message)

        if(GeneralFishing.rareDropPartyChat) {
            sendPartyMessage(message.toUnformattedString())
        }
    }
}