package cloud.glitchdev.rfu.feature.fishing

import cloud.glitchdev.rfu.RiccioFishingUtils.mc
import cloud.glitchdev.rfu.config.categories.GeneralFishing
import cloud.glitchdev.rfu.constants.text.TextColor
import cloud.glitchdev.rfu.events.managers.ItemUsedEvents.registerItemUsedEvent
import cloud.glitchdev.rfu.feature.Feature
import cloud.glitchdev.rfu.feature.RFUFeature
import cloud.glitchdev.rfu.utils.Title
import cloud.glitchdev.rfu.utils.dsl.isFishingRod
import cloud.glitchdev.rfu.utils.dsl.hasDescriptionText

@RFUFeature
object MissingRodPieceAlert : Feature {
    override fun onInitialize() {
        registerItemUsedEvent { item ->
            if (!GeneralFishing.missingRodPieceAlert) return@registerItemUsedEvent
            if (!FishingSession.isFishing) return@registerItemUsedEvent
            if (!item.isFishingRod()) return@registerItemUsedEvent
            if (mc.player?.fishing != null) return@registerItemUsedEvent

            val missingPieces = mutableListOf<String>()
            if (item.hasDescriptionText("Hook NONE")) missingPieces.add("Hook")
            if (item.hasDescriptionText("Line NONE")) missingPieces.add("Line")
            if (item.hasDescriptionText("Sinker NONE")) missingPieces.add("Sinker")

            if (missingPieces.isNotEmpty()) {
                val subtitle = "${TextColor.GRAY}" + missingPieces.joinToString(", ")
                Title.showTitle("§c§lMISSING ROD PIECE!", subtitle)
            }
        }
    }
}
