package cloud.glitchdev.rfu.config.categories

import cloud.glitchdev.rfu.config.Category

object HotSpotSettings : Category("Hot Spots") {
    init {
        dualSeparator {
            title = "Visuals"
            description = "Make hotspots easier to see!"
        }
    }

    var highlightHotSpots by boolean(true) {
        name = Literal("Highlight Hot Spots")
        description = Literal("Makes hotspots highlighted and hides the particles")
    }

    init {
        dualSeparator {
            title = "Alerts"
            description = "Be notified about hotspot events"
        }
    }

    var hotspotExpiredAlert by boolean(true) {
        name = Literal("Hotspot Expired Alert")
        description = Literal("Sends an alert when the hotspot you're fishing in expires!")
    }

    var hotspotExpiredSound by observable(boolean(true) {
        name = Literal("Hotspot Expired Sound")
        description = Literal("Plays a sound when the hotspot you're fishing in expires!")
        condition = { hotspotExpiredAlert }
    }) { _, _ ->
        reloadScreen()
    }

    var hotspotExpiredVolume by float(1f) {
        name = Literal("Sound Volume")
        description = Literal("The volume for the hotspot expired sound")
        range = 0f..1f
        slider = true
        condition = { hotspotExpiredAlert && hotspotExpiredSound }
    }
}