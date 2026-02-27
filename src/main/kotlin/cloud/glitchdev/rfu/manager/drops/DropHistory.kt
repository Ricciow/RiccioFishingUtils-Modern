package cloud.glitchdev.rfu.manager.drops

import cloud.glitchdev.rfu.constants.Dyes
import cloud.glitchdev.rfu.constants.RareDrops
import cloud.glitchdev.rfu.manager.catches.CatchTracker.catchHistory

class DropHistory {
    var drops : MutableList<DropEntry> = mutableListOf()
    var dyeDrops : MutableList<DyeDropEntry> = mutableListOf()

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

        val count = if (drop.relatedScs.isEmpty()) null else drop.relatedScs.sumOf { sc ->
            catchHistory.getOrAdd(sc).total
        }

        dropEntry.addDrop(count, magicFind)
    }

    fun getOrAdd(drop : Dyes): DyeDropEntry {
        // dyeDrops can be null when loaded from an old save by Gson
        if (dyeDrops == null) dyeDrops = mutableListOf()
        val existing = dyeDrops.find { it.type == drop }

        if (existing != null) {
            return existing
        }

        val newEntry = DyeDropEntry(drop)

        dyeDrops.add(newEntry)
        return newEntry
    }

    fun registerDrop(drop : Dyes, magicFind: Int? = null) {
        if (dyeDrops == null) dyeDrops = mutableListOf()
        val dropEntry = getOrAdd(drop)

        val count = if (drop.relatedScs.isEmpty()) null else drop.relatedScs.sumOf { sc ->
            catchHistory.getOrAdd(sc).total
        }

        dropEntry.addDrop(count, magicFind)
    }

    class DropEntry(
        var type : RareDrops,
    ) {
        var history: MutableList<DropRecord> = mutableListOf()

        fun addDrop(count : Int?, magicFind : Int? = null) {
            val lastCount = history.lastOrNull()?.totalCount ?: 0
            val sinceCount = count?.let { it - lastCount }

            history.add(DropRecord(count ?: 0, sinceCount, magicFind))
        }
    }

    class DyeDropEntry(
        var type : Dyes,
    ) {
        var history: MutableList<DropRecord> = mutableListOf()

        fun addDrop(count : Int?, magicFind : Int? = null) {
            val lastCount = history.lastOrNull()?.totalCount ?: 0
            val sinceCount = count?.let { it - lastCount }

            history.add(DropRecord(count ?: 0, sinceCount, magicFind))
        }
    }
}