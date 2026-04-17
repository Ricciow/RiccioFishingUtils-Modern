package cloud.glitchdev.rfu.data.fishing

import cloud.glitchdev.rfu.constants.FishingIslands
import cloud.glitchdev.rfu.constants.HotSpotConstants
import cloud.glitchdev.rfu.constants.LiquidTypes
import cloud.glitchdev.rfu.utils.JsonFile
import net.minecraft.core.BlockPos

object HotspotCache {
    private val storage = JsonFile(
        filename = "hotspots.json",
        type = CacheStorage::class.java,
        defaultFactory = { CacheStorage() }
    )

    private val cache: MutableMap<String, HotspotData>
        get() = storage.data.hotspots

    fun addMeasurement(pos: BlockPos, distance: Double, liquid: LiquidTypes, buff: String, island: FishingIslands?) {
        val key = "${island?.island}_${pos.x},${pos.y},${pos.z}"
        val data = synchronized(cache) {
            cache.getOrPut(key) { HotspotData(liquid, island) }
        }

        data.liquid = liquid
        data.island = island
        data.lastMetadataUpdate = System.currentTimeMillis()
        
        if (buff.isNotEmpty()) {
            data.setSessionBuff(buff)
        }

        val distances = data.sessionDistances
        synchronized(distances) {
            if (distances.isEmpty() && data.radius > 0) {
                repeat(HotSpotConstants.INITIAL_MEASUREMENT_PADDING) { distances.add(data.radius.toDouble()) }
            }

            distances.add(distance)
            if (distances.size > HotSpotConstants.MAX_SESSION_MEASUREMENTS) {
                distances.removeAt(0)
            }
        }
    }

    fun getMedian(pos: BlockPos, island: FishingIslands?): Float? {
        val key = "${island?.island}_${pos.x},${pos.y},${pos.z}"
        val data = synchronized(cache) { cache[key] } ?: return null
        
        val distances = data.sessionDistances
        return synchronized(distances) {
            if (distances.isEmpty()) return@synchronized data.radius
            val sorted = distances.sorted()
            (sorted[sorted.size / 2]).toFloat()
        }
    }

    fun getRadius(pos: BlockPos, island: FishingIslands?): Float? {
        val key = "${island?.island}_${pos.x},${pos.y},${pos.z}"
        return synchronized(cache) {
            cache[key]?.radius
        }
    }

    fun updateRadius(pos: BlockPos, radius: Float, island: FishingIslands?) {
        val key = "${island?.island}_${pos.x},${pos.y},${pos.z}"
        synchronized(cache) {
            val data = cache[key] ?: return@synchronized
            data.radius = radius
        }
    }

    fun getCachedEntries(island: FishingIslands?): List<Pair<BlockPos, HotspotData>> {
        return synchronized(cache) {
            cache.entries.mapNotNull { (key, data) ->
                if (data.island != island) return@mapNotNull null
                // Key format is "ISLAND_X,Y,Z"
                val coordsPart = key.substringAfterLast('_')
                val coords = coordsPart.split(",")
                if (coords.size != 3) return@mapNotNull null
                val pos = BlockPos(coords[0].toInt(), coords[1].toInt(), coords[2].toInt())
                pos to data
            }
        }
    }
}
