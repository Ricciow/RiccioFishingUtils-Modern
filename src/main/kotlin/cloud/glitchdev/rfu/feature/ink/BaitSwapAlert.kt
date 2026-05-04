package cloud.glitchdev.rfu.feature.ink

import cloud.glitchdev.rfu.RiccioFishingUtils.mc
import cloud.glitchdev.rfu.config.categories.InkFishing
import cloud.glitchdev.rfu.constants.FishingIslands
import cloud.glitchdev.rfu.events.managers.TickEvents.registerTickEvent
import cloud.glitchdev.rfu.feature.Feature
import cloud.glitchdev.rfu.feature.RFUFeature
import cloud.glitchdev.rfu.utils.Chat
import cloud.glitchdev.rfu.utils.Sounds
import cloud.glitchdev.rfu.utils.Title
import cloud.glitchdev.rfu.utils.World
import net.minecraft.network.chat.Component

@RFUFeature
object BaitSwapAlert : Feature {
    private var isNightSwap = false
    private var isDaySwap = false

    override fun onInitialize() {
        registerTickEvent(interval = 20) {
            if(!InkFishing.baitAlert) return@registerTickEvent

            if(World.island == FishingIslands.PARK) {
                val currentHour = World.SBHour
                val currentMinute = World.SBMinute

                isNightSwap = false
                isDaySwap = false


                if(currentHour == 18 && currentMinute == 50) { // right before it becomes night
                    isNightSwap = true
                } else if(currentHour == 5 && currentMinute == 50) {
                    isDaySwap = true
                } else {
                    return@registerTickEvent
                }

                if(isNightSwap) {

                    Title.showTitle("§4§lNight Time!", "§7Swap to Dark Bait!", 10, 20, 10)
                    if (InkFishing.baitAlertSound) {
                        Sounds.playSound("rfu:rain_expired", 1f, InkFishing.baitAlertVolume)
                    }
                } else {
                    Title.showTitle("§e§lDay Time!", "§7Swap to Carrot Bait!", 10, 20, 10)
                    if (InkFishing.baitAlertSound) {
                        Sounds.playSound("rfu:rain_expired", 1f, InkFishing.baitAlertVolume)
                    }
                }


            }
        }
    }
}