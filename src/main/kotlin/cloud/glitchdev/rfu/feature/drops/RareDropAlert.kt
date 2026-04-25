package cloud.glitchdev.rfu.feature.drops

import cloud.glitchdev.rfu.config.categories.DropsSettings
import cloud.glitchdev.rfu.constants.IRareDrop
import cloud.glitchdev.rfu.data.drops.DropManager
import cloud.glitchdev.rfu.data.drops.DropRecord
import cloud.glitchdev.rfu.events.managers.DropEvents
import cloud.glitchdev.rfu.feature.Feature
import cloud.glitchdev.rfu.feature.RFUFeature
import cloud.glitchdev.rfu.utils.Chat.sendMessage
import cloud.glitchdev.rfu.utils.Chat.sendPartyMessage
import cloud.glitchdev.rfu.utils.Coroutines
import cloud.glitchdev.rfu.utils.Title
import cloud.glitchdev.rfu.utils.dsl.formatTemplate
import cloud.glitchdev.rfu.utils.dsl.toReadableString
import gg.essential.universal.utils.toUnformattedString
import kotlinx.coroutines.delay
import net.minecraft.network.chat.Component

@RFUFeature
object RareDropAlert : Feature {
    override fun onInitialize() {
        DropEvents.registerRareDropEvent { rareDrop, magicFind ->
            if (rareDrop !in DropsSettings.rareDrops) return@registerRareDropEvent true
            
            val history = DropManager.dropHistory.getOrAdd(rareDrop).history
            handleAlert(rareDrop, history, magicFind)
            
            return@registerRareDropEvent !DropsSettings.customRareDropMessage
        }

        DropEvents.registerDyeDropEvent { dyeDrop, magicFind ->
            if (dyeDrop !in DropsSettings.dyeDrops) return@registerDyeDropEvent
            
            val history = DropManager.dropHistory.getOrAdd(dyeDrop).history
            handleAlert(dyeDrop, history, magicFind)
        }
    }

    private fun handleAlert(drop: IRareDrop, history: List<DropRecord>, magicFind: Int?) {
        val currentDrop = history.lastOrNull() ?: return
        val previousDrop = if (history.lastIndex - 1 >= 0) history[history.lastIndex - 1] else null

        val timeSinceLast = if (previousDrop != null) {
            (currentDrop.date - previousDrop.date).toReadableString()
        } else {
            "First Drop"
        }

        val placeholders = arrayOf(
            "drop" to drop.displayName,
            "dropcolor" to drop.rarity.color.code,
            "magic_find" to (magicFind?.toString() ?: "0"),
            "count" to (currentDrop.sinceCount?.toString() ?: "N/A"),
            "time" to timeSinceLast,
            "total" to history.size.toString()
        )

        // Title Alert
        if (DropsSettings.rareDropTitleAlert) {
            val title = DropsSettings.rareDropTitleFormat.formatTemplate(*placeholders)
            val subtitle = DropsSettings.rareDropSubtitleFormat.formatTemplate(*placeholders)
            Title.showTitle(title, subtitle, fadeIn = 5, duration = 40, fadeOut = 5)
        }

        // Chat Message
        if (DropsSettings.customRareDropMessage) {
            val messageString = DropsSettings.rareDropMessageFormat.formatTemplate(*placeholders)
            val message = Component.literal(messageString)

            Coroutines.launch {
                delay(100)
                sendMessage(message)
            }

            if (DropsSettings.rareDropPartyChat) {
                sendPartyMessage(message.toUnformattedString())
            }
        }
    }

    fun previewTitle() {
        preview(isTitle = true)
    }

    fun previewMessage() {
        preview(isTitle = false)
    }

    private fun preview(isTitle: Boolean) {
        val placeholders = arrayOf(
            "drop" to "Radioactive Vial",
            "dropcolor" to "&d",
            "magic_find" to "350",
            "count" to "100",
            "time" to "5m 20s",
            "total" to "500"
        )

        if (isTitle) {
            val title = DropsSettings.rareDropTitleFormat.formatTemplate(*placeholders)
            val subtitle = DropsSettings.rareDropSubtitleFormat.formatTemplate(*placeholders)
            Title.showTitle(title, subtitle, fadeIn = 5, duration = 40, fadeOut = 5)
        } else {
            val preview = DropsSettings.rareDropMessageFormat.formatTemplate(*placeholders)
            sendMessage(Component.literal(preview))
        }
    }
}
