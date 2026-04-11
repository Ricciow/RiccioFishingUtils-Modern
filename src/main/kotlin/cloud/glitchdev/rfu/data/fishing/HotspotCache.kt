package cloud.glitchdev.rfu.data.fishing

import cloud.glitchdev.rfu.constants.FishingIslands
import cloud.glitchdev.rfu.constants.LiquidTypes
import cloud.glitchdev.rfu.utils.JsonFile
import net.minecraft.core.BlockPos

object HotspotCache {
    private const val MAX_SESSION_MEASUREMENTS = 250

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
                repeat(25) { distances.add(data.radius.toDouble()) }
            }

            distances.add(distance)
            if (distances.size > MAX_SESSION_MEASUREMENTS) {
                distances.removeAt(0)
            }

            val sorted = distances.sorted()
            data.radius = (sorted[sorted.size / 2]).toFloat()
        }
    }

    fun getMedian(pos: BlockPos, island: FishingIslands?): Float? {
        val key = "${island?.island}_${pos.x},${pos.y},${pos.z}"
        return synchronized(cache) {
            cache[key]?.radius
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
