package cloud.glitchdev.rfu.constants

//Used in HotspotEvents.kt, Hotspot.kt and HotspotCache.kt
object HotSpotConstants {
    const val STABILITY_TIME_MS = 1000L
    const val MEDIAN_STABILITY_TOLERANCE = 0.001f
    const val METADATA_EXPIRY_MS = 30000L
    
    const val RANGE_DISPOSE_DISTANCE = 25.0
    const val RANGE_ENTRY_TIMEOUT_MS = 2000L
    const val INACTIVITY_TIMEOUT_MS = 200L
    const val EXTERNAL_TIMEOUT_MS = 300000L // 5 minutes
    
    const val PARTICLE_MAX_VERTICAL_DISTANCE = 6.0
    const val PARTICLE_MAX_HORIZONTAL_DISTANCE = 6.0
    const val PARTICLE_DETECTION_RADIUS = 6.0
    
    const val VIRTUAL_PARTICLE_THRESHOLD = 3
    const val RADIUS_CANCELLATION_TOLERANCE = 0.05
    
    const val BUFF_SEARCH_HORIZONTAL = 0.5
    const val BUFF_SEARCH_VERTICAL = 4.0
    const val LIQUID_SEARCH_HORIZONTAL = 1
    const val LIQUID_SEARCH_VERTICAL_MIN = -4
    const val LIQUID_SEARCH_VERTICAL_MAX = 1
    
    const val MAX_SESSION_MEASUREMENTS = 250
    const val INITIAL_MEASUREMENT_PADDING = 25

    // Hotspot Particle Color (Hot Pink/Magenta-ish)
    const val PARTICLE_RED = 255
    const val PARTICLE_GREEN = 105
    const val PARTICLE_BLUE = 180
}
