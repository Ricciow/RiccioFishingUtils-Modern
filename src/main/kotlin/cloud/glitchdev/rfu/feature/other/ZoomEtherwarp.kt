package cloud.glitchdev.rfu.feature.other

import cloud.glitchdev.rfu.RiccioFishingUtils.mc
import cloud.glitchdev.rfu.config.categories.OtherSettings
import cloud.glitchdev.rfu.events.managers.FovEvents.registerFovEvent
import cloud.glitchdev.rfu.events.managers.TickEvents.registerTickEvent
import cloud.glitchdev.rfu.feature.Feature
import cloud.glitchdev.rfu.feature.RFUFeature
import cloud.glitchdev.rfu.utils.dsl.hasDescriptionText

@RFUFeature
object ZoomEtherwarp : Feature {
    private var zoomProgress = 0.0f
    private var lastZoomProgress = 0.0f

    override fun onInitialize() {
        registerTickEvent {
            lastZoomProgress = zoomProgress
            val shouldZoom = OtherSettings.zoomEtherwarp &&
                    (mc.player?.isShiftKeyDown ?: false) &&
                    (mc.player?.mainHandItem?.hasDescriptionText("Ether Transmission") ?: false)

            val step = 0.15f
            if (shouldZoom) {
                zoomProgress = (zoomProgress + step).coerceAtMost(1.0f)
            } else {
                zoomProgress = (zoomProgress - step).coerceAtLeast(0.0f)
            }
        }

        registerFovEvent { cancelable, partialTick ->
            if (zoomProgress == 0.0f && lastZoomProgress == 0.0f) return@registerFovEvent
            
            val lerpedProgress = lastZoomProgress + (zoomProgress - lastZoomProgress) * partialTick
            // Cubic Ease-In
            val easedProgress = lerpedProgress * lerpedProgress * lerpedProgress
            
            val zoomMultiplier = 1.0f + (0.3f - 1.0f) * easedProgress
            cancelable.cancel(cancelable.getValue() * zoomMultiplier)
        }
    }
}