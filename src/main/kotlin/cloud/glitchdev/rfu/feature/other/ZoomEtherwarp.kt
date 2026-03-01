package cloud.glitchdev.rfu.feature.other

import cloud.glitchdev.rfu.RiccioFishingUtils.mc
import cloud.glitchdev.rfu.config.categories.OtherSettings
import cloud.glitchdev.rfu.events.managers.FovEvents.registerFovEvent
import cloud.glitchdev.rfu.feature.Feature
import cloud.glitchdev.rfu.feature.RFUFeature
import cloud.glitchdev.rfu.utils.dsl.hasDescriptionText

@RFUFeature
object ZoomEtherwarp : Feature {
    override fun onInitialize() {
        registerFovEvent { cancelable ->
            if(!OtherSettings.zoomEtherwarp) return@registerFovEvent
            if(!(mc.player?.isShiftKeyDown ?: false)) return@registerFovEvent
            val heldItem = mc.player?.mainHandItem ?: return@registerFovEvent
            if(!heldItem.hasDescriptionText("Ether Transmission")) return@registerFovEvent

            cancelable.cancel(cancelable.getValue() * 0.3f)
        }
    }
}