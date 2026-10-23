package cloud.glitchdev.rfu.data.drops

import cloud.glitchdev.rfu.constants.skyblock.Dyes
import cloud.glitchdev.rfu.constants.fishing.RareDrops
import cloud.glitchdev.rfu.constants.fishing.SeaCreatures
import cloud.glitchdev.rfu.data.drops.migration.DropMigration
import kotlin.time.Clock
import kotlin.time.Instant

class DropHistory {
    var version: Int = DropMigration.CURRENT_VERSION
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

    fun registerDrop(drop : RareDrops, magicFind: Int? = null, mobName: String? = null, date: Instant = Clock.System.now()) {
        val dropEntry = getOrAdd(drop)

        val count = if (drop.relatedScs.isEmpty()) null else dropEntry.currentCount

        dropEntry.addDrop(count, magicFind, date, mobName)
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

    fun registerDrop(drop : Dyes, magicFind: Int? = null, mobName: String? = null, date: Instant = Clock.System.now()) {
        @Suppress("SENSELESS_COMPARISON")
        if (dyeDrops == null) dyeDrops = mutableListOf()
        val dropEntry = getOrAdd(drop)

        val count = if (drop.relatedScs.isEmpty()) null else dropEntry.currentCount

        dropEntry.addDrop(count, magicFind, date, mobName)
    }

    fun registerCatch(sc: SeaCreatures, amount: Int = 1) {
        RareDrops.entries.filter { sc in it.relatedScs }.forEach {
            val entry = getOrAdd(it)
            entry.currentCount += amount
        }
        Dyes.entries.filter { sc in it.relatedScs }.forEach {
            val entry = getOrAdd(it)
            entry.currentCount += amount
        }
    }

    interface IDropEntry {
        val history: MutableList<DropRecord>
        var currentCount: Int

        fun addDrop(count : Int?, magicFind : Int? = null, date: Instant = Clock.System.now(), mobName: String? = null, resetCounter: Boolean = true) {
            val record = DropRecord(count, magicFind, mobName)
            record.date = date
            history.add(record)
            history.sortBy { it.date }
            if (resetCounter) currentCount = 0
        }

        fun removeDrop(displayIndex: Int): DropRecord? {
            if (displayIndex !in 1..history.size) return null
            val targetIndex = history.size - displayIndex
            val removed = history.removeAt(targetIndex)
            if (displayIndex == 1) {
                currentCount += removed.sinceCount ?: 0
            } else {
                val next = history[targetIndex]
                next.sinceCount = next.sinceCount?.let { nextCount ->
                    removed.sinceCount?.let { nextCount + it }
                }
            }
            return removed
        }
    }

    class DropEntry(
        var type : RareDrops,
    ) : IDropEntry {
        override var history: MutableList<DropRecord> = mutableListOf()
        override var currentCount: Int = 0
    }

    class DyeDropEntry(
        var type : Dyes,
    ) : IDropEntry {
        override var history: MutableList<DropRecord> = mutableListOf()
        override var currentCount: Int = 0
    }
}
