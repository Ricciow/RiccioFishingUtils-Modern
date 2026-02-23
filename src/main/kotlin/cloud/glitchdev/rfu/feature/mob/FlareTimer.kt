package cloud.glitchdev.rfu.feature.mob

import cloud.glitchdev.rfu.config.categories.GeneralFishing
import cloud.glitchdev.rfu.events.managers.ChatEvents.registerGameEvent
import cloud.glitchdev.rfu.events.managers.TickEvents.registerTickEvent
import cloud.glitchdev.rfu.feature.Feature
import cloud.glitchdev.rfu.feature.RFUFeature
import cloud.glitchdev.rfu.gui.hud.elements.FlareTimerDisplay
import cloud.glitchdev.rfu.manager.mob.DeployableManager
import cloud.glitchdev.rfu.utils.Title
import cloud.glitchdev.rfu.utils.dsl.toExactRegex
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

@RFUFeature
object FlareTimer : Feature {
    var lastRemaining : Duration? = null

    override fun onInitialize() {
        registerTickEvent(interval = 20) {
            val endTime = DeployableManager.activeFlareEndTime
            val flareType = DeployableManager.activeFlareType
            if (endTime != null) {
                val remainingMillis = endTime - System.currentTimeMillis()
                if (remainingMillis > 0) {
                    updateTime(remainingMillis.milliseconds, flareType)
                } else {
                    updateTime(Duration.ZERO, flareType)
                }
            } else {
                updateTime(null)
            }
        }

        registerGameEvent("Your flare disappeared because you were too far away!".toExactRegex()) { _, _, _ ->
            DeployableManager.resetFlare()
        }
    }

    private fun updateTime(remaining : Duration?, type : DeployableManager.FlareType = DeployableManager.FlareType.NONE) {
        FlareTimerDisplay.updateTime(remaining, type)

        if(
            GeneralFishing.flareAlert &&
            lastRemaining != null && remaining == null
        ) {
            Title.showTitle("§6§lFlare Expired!") { lastRemaining != null }
        }

        lastRemaining = remaining
    }
}
