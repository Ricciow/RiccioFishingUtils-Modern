package cloud.glitchdev.rfu.gui.components.elementa.group

import cloud.glitchdev.rfu.utils.gui.isDeepHidden
import gg.essential.elementa.UIComponent
import gg.essential.elementa.constraints.ConstraintType
import java.lang.ref.WeakReference
import java.util.EnumMap

internal object GroupManager {
    private val groups = mutableMapOf<String, Group>()

    fun getOrCreateGroup(key: String): Group = groups.getOrPut(key) { Group(key) }
    fun clearGroup(key: String) { groups.remove(key) }
    fun clearAll() { groups.clear() }
}

internal class Group(val key: String) {
    private val members = mutableMapOf<GroupMaxSizeConstraint, WeakReference<UIComponent>>()
    private val caches = EnumMap<ConstraintType, FrameCache>(ConstraintType::class.java)

    fun register(constraint: GroupMaxSizeConstraint, component: UIComponent) {
        if (!members.containsKey(constraint)) {
            members[constraint] = WeakReference(component)
            invalidateCaches()
        }
    }

    fun getMaxValue(type: ConstraintType, frameTime: Long, currentConstraint: GroupMaxSizeConstraint, currentComponent: UIComponent): Float {
        val cache = caches.getOrPut(type) { FrameCache() }
        if (cache.lastFrameTime == frameTime) {
            return cache.value
        }

        return calculateAndCache(type, frameTime, cache, currentConstraint, currentComponent)
    }

    private fun calculateAndCache(
        type: ConstraintType,
        frameTime: Long,
        cache: FrameCache,
        currentConstraint: GroupMaxSizeConstraint,
        currentComponent: UIComponent
    ): Float {
        val iterator = members.entries.iterator()
        var max = currentConstraint.getBaseValue(currentComponent, type)

        while (iterator.hasNext()) {
            val entry = iterator.next()
            val constraint = entry.key
            val component = entry.value.get()

            if (component == null || component.isDeepHidden()) {
                iterator.remove()
                continue
            }

            if (constraint === currentConstraint) continue

            val value = constraint.getBaseValue(component, type)
            if (value > max) {
                max = value
            }
        }

        cache.lastFrameTime = frameTime
        cache.value = max
        return max
    }

    private fun invalidateCaches() {
        caches.values.forEach { it.lastFrameTime = -1 }
    }

    private fun <K : Enum<K>, V> EnumMap<K, V>.getOrPut(key: K, defaultValue: () -> V): V {
        return this[key] ?: defaultValue().also { this[key] = it }
    }
}
