package cloud.glitchdev.rfu.feature.drops

import cloud.glitchdev.rfu.config.categories.GeneralFishing
import cloud.glitchdev.rfu.events.managers.DropEvents
import cloud.glitchdev.rfu.feature.Feature
import cloud.glitchdev.rfu.feature.RFUFeature
import cloud.glitchdev.rfu.manager.drops.DropManager
import cloud.glitchdev.rfu.manager.drops.DropRecord
import cloud.glitchdev.rfu.utils.Chat.sendMessage
import cloud.glitchdev.rfu.utils.Chat.sendPartyMessage
import cloud.glitchdev.rfu.utils.dsl.toMcCodes
import cloud.glitchdev.rfu.utils.dsl.toReadableString
import gg.essential.universal.utils.toUnformattedString
import net.minecraft.network.chat.Component

@RFUFeature
object CustomRareDropMessage : Feature {
    override fun onInitialize() {
        DropEvents.registerRareDropEvent { rareDrop, magicFind ->
            sendCustomDropMessage(rareDrop.dropName, DropManager.dropHistory.getOrAdd(rareDrop).history, magicFind)
        }

        DropEvents.registerDyeDropEvent { dyeDrop, magicFind ->
            sendCustomDropMessage(dyeDrop.dyeName, DropManager.dropHistory.getOrAdd(dyeDrop).history, magicFind)
        }
    }

    private fun sendCustomDropMessage(dropName: String, history: List<DropRecord>, magicFind: Int?) {
        if (!GeneralFishing.customRareDropMessage) return

        val currentDrop = history.lastOrNull() ?: return
        val previousDrop = if (history.lastIndex - 1 >= 0) history[history.lastIndex - 1] else null

        val timeSinceLast = if (previousDrop != null) {
            (currentDrop.date - previousDrop.date).toReadableString()
        } else {
            "First Drop"
        }

        val messageString = GeneralFishing.rareDropMessageFormat
            .replace("{drop}", dropName)
            .replace("{magic_find}", magicFind?.toString() ?: "0")
            .replace("{count}", currentDrop.sinceCount?.toString() ?: "N/A")
            .replace("{time}", timeSinceLast)
            .toMcCodes()

        val message = Component.literal(messageString)

        sendMessage(message)

        if (GeneralFishing.rareDropPartyChat) {
            sendPartyMessage(message.toUnformattedString())
        }
    }
}