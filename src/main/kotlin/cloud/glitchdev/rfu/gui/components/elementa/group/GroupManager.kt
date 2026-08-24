package cloud.glitchdev.rfu.gui.components.elementa.group

import gg.essential.elementa.constraints.ConstraintType
import java.util.EnumMap
import java.util.concurrent.ConcurrentHashMap

internal object GroupManager {
    private val groups = ConcurrentHashMap<String, Group>()

    fun getOrCreateGroup(key: String): Group = groups.computeIfAbsent(key) { Group(key) }
    fun clearGroup(key: String) { groups.remove(key) }
    fun clearAll() { groups.clear() }
}

internal class Group(val key: String) {
    private val caches = EnumMap<ConstraintType, FrameCache>(ConstraintType::class.java).apply {
        for (type in ConstraintType.entries) {
            put(type, FrameCache())
        }
    }

    fun getMaxValue(type: ConstraintType, frameTime: Long, baseValue: Float): Float {
        val cache = caches[type] ?: return baseValue
        return cache.updateAndGetMax(frameTime, baseValue)
    }

    fun clear() {
        caches.values.forEach { it.clear() }
    }
}
