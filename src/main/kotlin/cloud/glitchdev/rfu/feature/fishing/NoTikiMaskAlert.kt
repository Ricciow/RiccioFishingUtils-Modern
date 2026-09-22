package cloud.glitchdev.rfu.feature.fishing

import cloud.glitchdev.rfu.RiccioFishingUtils.mc
import cloud.glitchdev.rfu.config.categories.HotSpotSettings
import cloud.glitchdev.rfu.events.managers.ArmorEvents
import cloud.glitchdev.rfu.events.managers.ArmorEvents.registerArmorChangeEvent
import cloud.glitchdev.rfu.events.managers.HotSpotEvents
import cloud.glitchdev.rfu.events.managers.ItemUsedEvents.registerItemUsedEvent
import cloud.glitchdev.rfu.feature.Feature
import cloud.glitchdev.rfu.feature.RFUFeature
import cloud.glitchdev.rfu.utils.Title
import cloud.glitchdev.rfu.utils.dsl.isFishingRod

@RFUFeature
object NoTikiMaskAlert : Feature {
    override fun onInitialize() {
        registerItemUsedEvent { item ->
            if (!HotSpotSettings.noTikiMaskAlert) return@registerItemUsedEvent
            if (!FishingSession.isFishing) return@registerItemUsedEvent
            if (!item.isFishingRod()) return@registerItemUsedEvent
            if (mc.player?.fishing != null) return@registerItemUsedEvent

            if (isFishingOnHotspot() && !ArmorEvents.currentArmorSet.isWearingTikiMask) {
                Title.showTitle("§c§lNO TIKI MASK!")
            }
        }
    }

    private fun isFishingOnHotspot(): Boolean {
        if (FishingSession.isHotspotFishing) return true
        val player = mc.player ?: return false
        val bobber = player.fishing
        if (bobber != null && HotSpotEvents.getHotspotAt(bobber.position()) != null) return true
        if (HotSpotEvents.getHotspotAt(player.position()) != null) return true
        return false
    }
}
