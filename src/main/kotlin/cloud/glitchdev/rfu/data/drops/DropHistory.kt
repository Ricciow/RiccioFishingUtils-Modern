package cloud.glitchdev.rfu.data.drops

import cloud.glitchdev.rfu.constants.skyblock.Dyes
import cloud.glitchdev.rfu.constants.fishing.RareDrops
import cloud.glitchdev.rfu.data.catches.CatchTracker.catchHistory
import kotlin.time.Clock
import kotlin.time.Instant

class DropHistory {
    var drops : MutableList<DropEntry> = mutableListOf()
    var dyeDrops : MutableList<DyeDropEntry> = mutableListOf()

    fun getOrAdd(drop : RareDrops): DropEntry {
        // Gson bypasses Kotlin null-safety and can insert nulls into non-nullable lists
        @Suppress("SENSELESS_COMPARISON")
        if (drops == null) drops = mutableListOf()
        @Suppress("UNCHECKED_CAST")
        drops = (drops as MutableList<DropEntry?>).filterNotNull().toMutableList()
        val existing = drops.find { it.type == drop }

        if (existing != null) {
            return existing
        }

        val newEntry = DropEntry(drop)

        drops.add(newEntry)
        return newEntry
    }

    fun registerDrop(drop : RareDrops, magicFind: Int? = null, date: Instant = Clock.System.now()) {
        val dropEntry = getOrAdd(drop)

        val count = if (drop.relatedScs.isEmpty()) null else drop.relatedScs.sumOf { sc ->
            catchHistory.getOrAdd(sc).total
        }

        dropEntry.addDrop(count, magicFind, date)
    }

    fun getOrAdd(drop : Dyes): DyeDropEntry {
        // dyeDrops can be null when loaded from an old save by Gson
        @Suppress("SENSELESS_COMPARISON")
        if (dyeDrops == null) dyeDrops = mutableListOf()
        // Gson bypasses Kotlin null-safety and can insert nulls into non-nullable lists
        @Suppress("UNCHECKED_CAST")
        dyeDrops = (dyeDrops as MutableList<DyeDropEntry?>).filterNotNull().toMutableList()
        val existing = dyeDrops.find { it.type == drop }

        if (existing != null) {
            return existing
        }

        val newEntry = DyeDropEntry(drop)

        dyeDrops.add(newEntry)
        return newEntry
    }

    fun registerDrop(drop : Dyes, magicFind: Int? = null, date: Instant = Clock.System.now()) {
        @Suppress("SENSELESS_COMPARISON")
        if (dyeDrops == null) dyeDrops = mutableListOf()
        val dropEntry = getOrAdd(drop)

        val count = if (drop.relatedScs.isEmpty()) null else drop.relatedScs.sumOf { sc ->
            catchHistory.getOrAdd(sc).total
        }

        dropEntry.addDrop(count, magicFind, date)
    }

    interface IDropEntry {
        val history: MutableList<DropRecord>

        fun addDrop(count : Int?, magicFind : Int? = null, date: Instant = Clock.System.now(), sinceCount: Int? = null) {
            val lastCount = history.lastOrNull()?.totalCount ?: 0
            val calculatedSince = sinceCount ?: count?.let { it - lastCount }
            val record = DropRecord(count ?: ((history.lastOrNull()?.totalCount ?: 0) + (sinceCount ?: 0)), calculatedSince, magicFind)
            record.date = date
            history.add(record)
            history.sortBy { it.date }
        }

        fun removeDrop(displayIndex: Int): DropRecord? {
            if (displayIndex !in 1..history.size) return null
            val targetIndex = history.size - displayIndex
            return history.removeAt(targetIndex)
        }
    }

    class DropEntry(
        var type : RareDrops,
    ) : IDropEntry {
        override var history: MutableList<DropRecord> = mutableListOf()
    }

    class DyeDropEntry(
        var type : Dyes,
    ) : IDropEntry {
        override var history: MutableList<DropRecord> = mutableListOf()
    }
}