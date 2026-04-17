package cloud.glitchdev.rfu.feature.drops

import cloud.glitchdev.rfu.config.categories.DropsSettings
import cloud.glitchdev.rfu.events.managers.DropEvents
import cloud.glitchdev.rfu.feature.Feature
import cloud.glitchdev.rfu.feature.RFUFeature
import cloud.glitchdev.rfu.data.drops.DropManager
import cloud.glitchdev.rfu.data.drops.DropRecord
import cloud.glitchdev.rfu.utils.Chat.sendMessage
import cloud.glitchdev.rfu.utils.Chat.sendPartyMessage
import cloud.glitchdev.rfu.utils.dsl.formatTemplate
import cloud.glitchdev.rfu.utils.dsl.toReadableString
import gg.essential.universal.utils.toUnformattedString
import net.minecraft.network.chat.Component

@RFUFeature
object CustomRareDropMessage : Feature {
    override fun onInitialize() {
        DropEvents.registerRareDropEvent { rareDrop, magicFind ->
            if (rareDrop !in DropsSettings.rareDrops || !DropsSettings.customRareDropMessage) return@registerRareDropEvent true
            sendCustomDropMessage(rareDrop.dropName, DropManager.dropHistory.getOrAdd(rareDrop).history, magicFind)
            return@registerRareDropEvent false
        }

        DropEvents.registerDyeDropEvent { dyeDrop, magicFind ->
            if (dyeDrop !in DropsSettings.dyeDrops || !DropsSettings.customRareDropMessage) return@registerDyeDropEvent
            sendCustomDropMessage(dyeDrop.dyeName, DropManager.dropHistory.getOrAdd(dyeDrop).history, magicFind)
        }
    }

    private fun sendCustomDropMessage(dropName: String, history: List<DropRecord>, magicFind: Int?) {
        if (!DropsSettings.customRareDropMessage) return

        val currentDrop = history.lastOrNull() ?: return
        val previousDrop = if (history.lastIndex - 1 >= 0) history[history.lastIndex - 1] else null

        val timeSinceLast = if (previousDrop != null) {
            (currentDrop.date - previousDrop.date).toReadableString()
        } else {
            "First Drop"
        }

        val messageString = DropsSettings.rareDropMessageFormat.formatTemplate(
            "drop" to dropName,
            "magic_find" to (magicFind?.toString() ?: "0"),
            "count" to (currentDrop.sinceCount?.toString() ?: "N/A"),
            "time" to timeSinceLast,
            "total" to history.size.toString()
        )
        
        val message = Component.literal(messageString)

        sendMessage(message)

        if (DropsSettings.rareDropPartyChat) {
            sendPartyMessage(message.toUnformattedString())
        }
    }

    fun preview() {
        val preview = DropsSettings.rareDropMessageFormat.formatTemplate(
            "drop" to "&dRadioactive Vial",
            "magic_find" to "350",
            "count" to "100",
            "time" to "5m 20s",
            "total" to "500"
        )

        sendMessage(Component.literal(preview))
    }
}