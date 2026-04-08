package cloud.glitchdev.rfu.feature.fishing

import cloud.glitchdev.rfu.config.categories.GeneralFishing
import cloud.glitchdev.rfu.config.categories.JerryFishing
import cloud.glitchdev.rfu.constants.FishingIslands
import cloud.glitchdev.rfu.events.managers.ChatEvents.registerGameEvent
import cloud.glitchdev.rfu.events.managers.TickEvents.registerTickEvent
import cloud.glitchdev.rfu.feature.Feature
import cloud.glitchdev.rfu.feature.RFUFeature
import cloud.glitchdev.rfu.gui.hud.elements.BlizzardTimerDisplay
import cloud.glitchdev.rfu.utils.Sounds
import cloud.glitchdev.rfu.utils.Title
import cloud.glitchdev.rfu.utils.World
import cloud.glitchdev.rfu.utils.dsl.toExactRegex

@RFUFeature
object BlizzardTimer : Feature {
    private val blizzardStartRegex = """BLIZZARD! (?:\[[\w+]+\] )?(\w+) opened a Blizzard in a Bottle, improving everyone's Fishing Stats for the next 10 minutes and causing it to snow!""".toExactRegex()
    private val blizzardActiveRegex = """The Blizzard in this area is boosting your Fishing stats!""".toExactRegex()
    private val blizzardEndRegex = """The Blizzard petered out\.\.\.""".toExactRegex()

    var endTimeMillis: Long = 0
        private set

    var blizzardActive: Boolean = false
        private set

    val isActive: Boolean
        get() = blizzardActive && World.island == FishingIslands.JERRY

    override fun onInitialize() {
        registerGameEvent(blizzardStartRegex) { _, _, _ ->
            if (World.island == FishingIslands.JERRY) {
                blizzardActive = true
                endTimeMillis = System.currentTimeMillis() + 600_000
                BlizzardTimerDisplay.updateState()
            }
        }

        registerGameEvent(blizzardActiveRegex) { _, _, _ ->
            if (World.island == FishingIslands.JERRY) {
                blizzardActive = true
                if (endTimeMillis <= System.currentTimeMillis()) {
                    endTimeMillis = 0
                }
                BlizzardTimerDisplay.updateState()
            }
        }

        registerGameEvent(blizzardEndRegex) { _, _, _ ->
            if (World.island == FishingIslands.JERRY) {
                if (blizzardActive && JerryFishing.blizzardExpiredAlert) {
                    Title.showTitle("§b§lBlizzard Expired!")
                    if (GeneralFishing.deployableExpiredSound) {
                        Sounds.playSound("rfu:blizzard_expired", 1f, GeneralFishing.deployableExpiredVolume)
                    }
                }
                blizzardActive = false
                endTimeMillis = 0
                BlizzardTimerDisplay.updateState()
            }
        }

        registerTickEvent(interval = 20) {
            if (World.island != FishingIslands.JERRY) {
                if (blizzardActive) {
                    blizzardActive = false
                    endTimeMillis = 0
                    BlizzardTimerDisplay.updateState()
                }
                return@registerTickEvent
            }

            if (blizzardActive) {
                BlizzardTimerDisplay.updateState()
            }
        }
    }
}
