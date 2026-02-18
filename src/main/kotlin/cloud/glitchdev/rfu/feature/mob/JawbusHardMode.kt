package cloud.glitchdev.rfu.feature.mob

import cloud.glitchdev.rfu.RiccioFishingUtils.mc
import cloud.glitchdev.rfu.config.categories.LavaFishing
import cloud.glitchdev.rfu.events.managers.ChatEvents.registerGameEvent
import cloud.glitchdev.rfu.feature.Feature
import cloud.glitchdev.rfu.feature.RFUFeature
import cloud.glitchdev.rfu.gui.window.DeadWindow
import cloud.glitchdev.rfu.utils.dsl.toExactRegex
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.minecraft.CrashReport

@RFUFeature
object JawbusHardMode : Feature {
    override fun onInitialize() {
        registerGameEvent(" ☠ You were killed by Lord Jawbus.".toExactRegex()) { _, _, _ ->
            if(LavaFishing.jawbus_hard_mode) {
                CoroutineScope(Dispatchers.Default).launch {
                    DeadWindow.open()

                    delay(2000)

                    mc.execute {
                        mc.emergencySaveAndCrash(CrashReport("You died to lord jawbus.", RuntimeException("Skill Issue")))
                    }
                }
            }
        }
    }
}