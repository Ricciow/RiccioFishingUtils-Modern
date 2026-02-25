package cloud.glitchdev.rfu.manager.catches

import cloud.glitchdev.rfu.config.categories.GeneralFishing
import cloud.glitchdev.rfu.constants.SeaCreatures
import kotlin.time.Clock
import kotlin.time.Instant

class CatchHistory {
    var catches: MutableList<CatchRecord> = ArrayList()

    /**
     * Finds the record for a specific SeaCreature.
     * If it doesn't exist, it creates it, adds it to the list, and returns the new one.
     */
    fun getOrAdd(sc: SeaCreatures): CatchRecord {
        val existing = catches.find { it.name == sc.scName }

        if (existing != null) {
            return existing
        }

        val newRecord = CatchRecord().apply {
            name = sc.scName
            total = 0
            count = 0
        }

        catches.add(newRecord)
        return newRecord
    }

    /**
     * Helper to easily update stats without manual fetching
     */
    fun registerCatch(sc: SeaCreatures) {
        val currentRecord = getOrAdd(sc)

        catches.forEach { record ->
            if(SeaCreatures.isInIslands(record.name, sc.category)) {
                record.count += 1
            }
        }
        if(GeneralFishing.rareSC.contains(sc)) {
            currentRecord.history.add(currentRecord.count)
        }
        currentRecord.time = Clock.System.now()
        currentRecord.count = 0
        currentRecord.total += 1
    }

    /**
     * Function to delete particularly large mob histories
     * @param maxSize The maximum number of history data allowed
     */
    fun cleanExcessData(maxSize : Int) {
        catches.forEach { record ->
            val excess = record.history.size - maxSize
            if (excess > 0) {
                record.history.subList(0, excess).clear()
            }
        }
    }

    class CatchRecord {
        var name: String = ""
        var total: Int = 0
        var count: Int = 0
        var time : Instant = Clock.System.now()
        var history: MutableList<Int> = ArrayList()
    }
}