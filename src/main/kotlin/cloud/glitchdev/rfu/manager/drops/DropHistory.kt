package cloud.glitchdev.rfu.manager.drops

import cloud.glitchdev.rfu.constants.RareDrops
import cloud.glitchdev.rfu.manager.catches.CatchTracker.catchHistory

class DropHistory {
    var drops : MutableList<DropEntry> = mutableListOf()

    fun getOrAdd(drop : RareDrops): DropEntry {
        val existing = drops.find { it.type == drop }

        if (existing != null) {
            return existing
        }

        val newEntry = DropEntry(drop)

        drops.add(newEntry)
        return newEntry
    }

    fun registerDrop(drop : RareDrops, magicFind: Int? = null) {
        val dropEntry = getOrAdd(drop)

        val count = drop.relatedScs.sumOf { sc ->
            catchHistory.getOrAdd(sc).count
        }

        dropEntry.addDrop(count, magicFind)
    }

    class DropEntry(
        var type : RareDrops,
    ) {
        var history: MutableList<DropRecord> = mutableListOf()

        fun addDrop(count : Int, magicFind : Int? = null) {
            history.add(DropRecord(count, magicFind))
        }
    }
}