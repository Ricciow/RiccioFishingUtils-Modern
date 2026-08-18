package cloud.glitchdev.rfu.feature.fishing

import cloud.glitchdev.rfu.RiccioFishingUtils.mc
import cloud.glitchdev.rfu.config.categories.GeneralFishing
import cloud.glitchdev.rfu.events.managers.ArmorEvents
import cloud.glitchdev.rfu.events.managers.ItemUsedEvents.registerItemUsedEvent
import cloud.glitchdev.rfu.feature.Feature
import cloud.glitchdev.rfu.feature.RFUFeature
import cloud.glitchdev.rfu.utils.Title
import cloud.glitchdev.rfu.utils.dsl.isFishingRod

@RFUFeature
object NoFishingArmorAlert : Feature {
    override fun onInitialize() {
        registerItemUsedEvent { item ->
            if (!GeneralFishing.noFishingArmorAlert) return@registerItemUsedEvent
            if (!FishingSession.isFishing) return@registerItemUsedEvent
            if (!item.isFishingRod()) return@registerItemUsedEvent
            if (mc.player?.fishing != null) return@registerItemUsedEvent

            if (!ArmorEvents.currentArmorSet.isWearingFishingArmor) {
                Title.showTitle("§c§lNO FISHING ARMOR!")
            }
        }
    }
}
