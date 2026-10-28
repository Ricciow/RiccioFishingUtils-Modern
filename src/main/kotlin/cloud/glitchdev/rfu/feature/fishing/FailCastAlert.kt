package cloud.glitchdev.rfu.feature.fishing

import cloud.glitchdev.rfu.config.categories.GeneralFishing
import cloud.glitchdev.rfu.RiccioFishingUtils.mc
import cloud.glitchdev.rfu.events.managers.EntityRemovedEvents.registerEntityRemovedEvent
import cloud.glitchdev.rfu.events.managers.ItemUsedEvents.registerItemUsedEvent
import cloud.glitchdev.rfu.feature.Feature
import cloud.glitchdev.rfu.feature.RFUFeature
import cloud.glitchdev.rfu.utils.Sounds

@RFUFeature
object FailCastAlert : Feature {
    private var isFishing = false
    private var sentAlert = false

    override fun onInitialize() {
        registerItemUsedEvent { item ->
            if(!GeneralFishing.failCast.enabled) return@registerItemUsedEvent
            if (item.item.descriptionId == "item.minecraft.fishing_rod") {
                sentAlert = false
                if (mc.player?.fishing == null) {
                    isFishing = true
                } else {
                    isFishing = false
                }
            }
        }

        registerEntityRemovedEvent { entityId ->
            if(!GeneralFishing.failCast.enabled) return@registerEntityRemovedEvent
            if(entityId != mc.player?.fishing?.id) return@registerEntityRemovedEvent
            if(!isFishing) return@registerEntityRemovedEvent
            if(mc.player?.mainHandItem?.item?.descriptionId != "item.minecraft.fishing_rod") return@registerEntityRemovedEvent
            isFishing = false
            if(!sentAlert) {
                sentAlert = true
                if(FishingSession.isFishing) {
                    GeneralFishing.failCast.show { !isFishing }
                    if(GeneralFishing.failCast.sound) {
                        Sounds.playSound("rfu:failed_cast", 1f, GeneralFishing.failCast.volume)
                    }
                }
            }
        }
    }
}
