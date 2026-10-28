package cloud.glitchdev.rfu.feature.other

import cloud.glitchdev.rfu.config.categories.OtherSettings
import cloud.glitchdev.rfu.RiccioFishingUtils.mc
import cloud.glitchdev.rfu.events.managers.TickEvents.registerTickEvent
import cloud.glitchdev.rfu.feature.Feature
import cloud.glitchdev.rfu.feature.RFUFeature
import cloud.glitchdev.rfu.utils.Sounds

@RFUFeature
object FullInventoryAlert : Feature {
    private var wasFull = false
    private var lastAlertTime = 0L

    override fun onInitialize() {
        registerTickEvent(interval = 10) {
            if (!OtherSettings.fullInventory.enabled) return@registerTickEvent
            val player = mc.player ?: return@registerTickEvent

            val isFull = player.inventory.freeSlot == -1

            if (isFull) {
                if (!wasFull || System.currentTimeMillis() - lastAlertTime > 10000L) {
                    wasFull = true
                    lastAlertTime = System.currentTimeMillis()

                    OtherSettings.fullInventory.show()

                    if (OtherSettings.fullInventory.sound) {
                        Sounds.playSound("rfu:inventory_full", 1f, OtherSettings.fullInventory.volume)
                    }
                }
            } else {
                wasFull = false
            }
        }
    }
}
