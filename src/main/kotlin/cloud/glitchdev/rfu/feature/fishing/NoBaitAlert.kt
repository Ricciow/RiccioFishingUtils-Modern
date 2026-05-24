package cloud.glitchdev.rfu.feature.fishing

import cloud.glitchdev.rfu.RiccioFishingUtils.mc
import cloud.glitchdev.rfu.config.categories.GeneralFishing
import cloud.glitchdev.rfu.events.managers.BaitEventManager
import cloud.glitchdev.rfu.events.managers.ItemUsedEvents.registerItemUsedEvent
import cloud.glitchdev.rfu.feature.Feature
import cloud.glitchdev.rfu.feature.RFUFeature
import cloud.glitchdev.rfu.utils.Title
import cloud.glitchdev.rfu.utils.dsl.isFishingRod

@RFUFeature
object NoBaitAlert : Feature {
    override fun onInitialize() {
        registerItemUsedEvent { item ->
            if (!GeneralFishing.noBaitAlert) return@registerItemUsedEvent
            if (!item.isFishingRod()) return@registerItemUsedEvent
            if (mc.player?.fishing != null) return@registerItemUsedEvent

            if (BaitEventManager.lastBait == null) {
                Title.showTitle("§c§lNO BAIT!")
            }
        }
    }
}
