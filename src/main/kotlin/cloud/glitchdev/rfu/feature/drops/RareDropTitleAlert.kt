package cloud.glitchdev.rfu.feature.drops
import cloud.glitchdev.rfu.config.categories.DropsSettings
import cloud.glitchdev.rfu.events.managers.DropEvents
import cloud.glitchdev.rfu.feature.Feature
import cloud.glitchdev.rfu.feature.RFUFeature
import cloud.glitchdev.rfu.constants.Rarity
import cloud.glitchdev.rfu.data.drops.DropManager
import cloud.glitchdev.rfu.data.drops.DropRecord
import cloud.glitchdev.rfu.utils.Title
import cloud.glitchdev.rfu.utils.dsl.toMcCodes
import cloud.glitchdev.rfu.utils.dsl.toReadableString

@RFUFeature
object RareDropTitleAlert : Feature {
    override fun onInitialize() {
        DropEvents.registerRareDropEvent { rareDrop, magicFind ->
            if (rareDrop !in DropsSettings.rareDrops || !DropsSettings.rareDropTitleAlert) return@registerRareDropEvent true
            showTitleAlert(rareDrop.dropName, rareDrop.rarity.color.code, DropManager.dropHistory.getOrAdd(rareDrop).history, magicFind)
            return@registerRareDropEvent true
        }

        DropEvents.registerDyeDropEvent { dyeDrop, magicFind ->
            if (dyeDrop !in DropsSettings.dyeDrops || !DropsSettings.rareDropTitleAlert) return@registerDyeDropEvent
            showTitleAlert(dyeDrop.dyeName, dyeDrop.rarity.color.code, DropManager.dropHistory.getOrAdd(dyeDrop).history, magicFind)
        }

    }

    private fun showTitleAlert(dropName: String, dropColor: String, history: List<DropRecord>, magicFind: Int?) {
        val currentDrop = history.lastOrNull() ?: return
        val previousDrop = if (history.lastIndex - 1 >= 0) history[history.lastIndex - 1] else null

        val timeSinceLast = if (previousDrop != null) {
            (currentDrop.date - previousDrop.date).toReadableString()
        } else {
            "First Drop"
        }

        val title = DropsSettings.rareDropTitleFormat
            .replace("{drop}", dropName)
            .replace("{dropcolor}", dropColor)
            .replace("{magic_find}", magicFind?.toString() ?: "0")
            .replace("{count}", currentDrop.sinceCount?.toString() ?: "N/A")
            .replace("{time}", timeSinceLast)
            .replace("{total}", history.size.toString())
            .toMcCodes()
        val subtitle = DropsSettings.rareDropSubtitleFormat
            .replace("{drop}", dropName)
            .replace("{dropcolor}", dropColor)
            .replace("{magic_find}", magicFind?.toString() ?: "0")
            .replace("{count}", currentDrop.sinceCount?.toString() ?: "N/A")
            .replace("{time}", timeSinceLast)
            .replace("{total}", history.size.toString())
            .toMcCodes()


        Title.showTitle(title, subtitle, fadeIn = 5, duration = 40, fadeOut = 5)
    }
}
