package cloud.glitchdev.rfu.config.categories

import cloud.glitchdev.rfu.config.Category

object HotSpotSettings : Category("Hot Spots") {
    var highlightHotSpots by boolean(true) {
        name = Literal("Highlight Hot Spots")
        description = Literal("Makes hotspots highlighted and hides the particles")
    }

    var hotspotExpiredAlert by boolean(true) {
        name = Literal("Hotspot Expired Alert")
        description = Literal("Sends an alert when the hotspot you're fishing in expires!")
    }
}