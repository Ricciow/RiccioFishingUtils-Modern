package cloud.glitchdev.rfu.feature.fishing

import cloud.glitchdev.rfu.RiccioFishingUtils.mc
import cloud.glitchdev.rfu.config.categories.GeneralFishing
import cloud.glitchdev.rfu.events.managers.EntityRemovedEvents.registerEntityRemovedEvent
import cloud.glitchdev.rfu.events.managers.ItemUsedEvents.registerItemUsedEvent
import cloud.glitchdev.rfu.feature.Feature
import cloud.glitchdev.rfu.feature.RFUFeature
import cloud.glitchdev.rfu.utils.Title
import cloud.glitchdev.rfu.utils.dsl.toMcCodes

@RFUFeature
object FailCastAlert : Feature {
    private var isFishing = false
    private var FAILED_CAST_MESSAGE = "&c&lFailed Cast!".toMcCodes()
    private var sentAlert = false

    override fun onInitialize() {
        registerItemUsedEvent { item ->
            if(!GeneralFishing.failCastAlert) return@registerItemUsedEvent
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
            if(!GeneralFishing.failCastAlert) return@registerEntityRemovedEvent
            if(entityId != mc.player?.fishing?.id) return@registerEntityRemovedEvent
            if(!isFishing) return@registerEntityRemovedEvent
            if(mc.player?.mainHandItem?.item?.descriptionId != "item.minecraft.fishing_rod") return@registerEntityRemovedEvent
            isFishing = false
            if(!sentAlert) {
                sentAlert = true
                Title.showTitle(FAILED_CAST_MESSAGE, "", 0, 5, 5) { !isFishing }
            }
        }
    }
}