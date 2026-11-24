package cloud.glitchdev.rfu.feature.fishing

import cloud.glitchdev.rfu.config.categories.GeneralFishing
import cloud.glitchdev.rfu.RiccioFishingUtils.mc
import cloud.glitchdev.rfu.events.EventScope
import cloud.glitchdev.rfu.events.managers.inventory.ArmorChangeEventManager
import cloud.glitchdev.rfu.events.managers.inventory.registerItemUsedEvent
import cloud.glitchdev.rfu.feature.Feature
import cloud.glitchdev.rfu.feature.RFUFeature
import cloud.glitchdev.rfu.utils.dsl.isFishingRod

@RFUFeature
object NoFishingArmorAlert : Feature {
    override fun onInitialize(scope: EventScope) {
        scope.registerItemUsedEvent { item ->
            if (!GeneralFishing.noFishingArmor.enabled) return@registerItemUsedEvent
            if (!FishingSession.isFishing) return@registerItemUsedEvent
            if (!item.isFishingRod()) return@registerItemUsedEvent
            if (mc.player?.fishing != null) return@registerItemUsedEvent

            if (!ArmorChangeEventManager.currentArmorSet.isWearingFishingArmor) {
                GeneralFishing.noFishingArmor.show()
            }
        }
    }
}
