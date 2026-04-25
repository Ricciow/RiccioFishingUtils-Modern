package cloud.glitchdev.rfu.data.fishing

import cloud.glitchdev.rfu.constants.FishingIslands
import cloud.glitchdev.rfu.constants.LiquidTypes
import java.util.Collections

data class HotspotData(
    var liquid: LiquidTypes,
    var island: FishingIslands?,
    var radius: Float = 0f
) {
    @Transient
    private var _sessionBuff: String? = ""
    val sessionBuff: String
        get() {
            if (_sessionBuff == null) _sessionBuff = ""
            return _sessionBuff!!
        }

    fun setSessionBuff(buff: String) {
        _sessionBuff = buff
    }

    fun clearSession() {
        _sessionBuff = ""
        _lastMetadataUpdate = 0L
        _sessionDistances?.clear()
    }

    @Transient
    private var _lastMetadataUpdate: Long? = 0L
    var lastMetadataUpdate: Long
        get() {
            if (_lastMetadataUpdate == null) _lastMetadataUpdate = 0L
            return _lastMetadataUpdate!!
        }
        set(value) {
            _lastMetadataUpdate = value
        }

    @Transient
    private var _sessionDistances: MutableList<Double>? = null
    val sessionDistances: MutableList<Double>
        get() {
            if (_sessionDistances == null) _sessionDistances = Collections.synchronizedList(mutableListOf())
            return _sessionDistances!!
        }
}

data class CacheStorage(
    val hotspots: MutableMap<String, HotspotData> = LinkedHashMap()
)
