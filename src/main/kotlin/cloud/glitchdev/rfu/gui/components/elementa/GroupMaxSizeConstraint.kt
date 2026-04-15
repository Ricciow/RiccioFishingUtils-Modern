package cloud.glitchdev.rfu.gui.components.elementa

import cloud.glitchdev.rfu.utils.gui.isHidden
import gg.essential.elementa.UIComponent
import gg.essential.elementa.constraints.ConstraintType
import gg.essential.elementa.constraints.SizeConstraint
import gg.essential.elementa.constraints.resolution.ConstraintVisitor
import gg.essential.elementa.dsl.pixel
import java.lang.ref.WeakReference

class GroupMaxSizeConstraint(
    val groupKey: String,
    val baseConstraint: SizeConstraint = 0.pixel
) : SizeConstraint {
    override var cachedValue = 0f
    override var recalculate = true
    override var constrainTo: UIComponent? = null

    private fun ensureRegistered(component: UIComponent) {
        val list = globalGroups.getOrPut(groupKey) { mutableListOf() }

        list.removeAll {
            val comp = it.second.get()
            comp == null || comp.isHidden()
        }

        if (!component.isHidden() && list.none { it.first === this && it.second.get() === component }) {
            list.add(Pair(this, WeakReference(component)))
        }
    }

    override fun getWidthImpl(component: UIComponent): Float {
        ensureRegistered(component)

        val group = globalGroups[groupKey] ?: return baseConstraint.getWidthImpl(component)

        return group.mapNotNull { (constraint, weakRef) ->
            val comp = weakRef.get()
            if (comp != null) {
                constraint.baseConstraint.getWidthImpl(comp)
            } else {
                null
            }
        }.maxOrNull() ?: 0f
    }

    override fun getHeightImpl(component: UIComponent): Float {
        ensureRegistered(component)

        val group = globalGroups[groupKey] ?: return baseConstraint.getHeightImpl(component)

        return group.mapNotNull { (constraint, weakRef) ->
            val comp = weakRef.get()
            if (comp != null) {
                constraint.baseConstraint.getHeightImpl(comp)
            } else {
                null
            }
        }.maxOrNull() ?: 0f
    }

    override fun getRadiusImpl(component: UIComponent): Float {
        ensureRegistered(component)

        val group = globalGroups[groupKey] ?: return baseConstraint.getRadiusImpl(component)

        return group.mapNotNull { (constraint, weakRef) ->
            val comp = weakRef.get()
            if (comp != null) {
                constraint.baseConstraint.getRadiusImpl(comp)
            } else {
                null
            }
        }.maxOrNull() ?: 0f
    }

    override fun visitImpl(visitor: ConstraintVisitor, type: ConstraintType) {
        baseConstraint.visitImpl(visitor, type)
    }

    companion object {
        private val globalGroups = mutableMapOf<String, MutableList<Pair<GroupMaxSizeConstraint, WeakReference<UIComponent>>>>()

        fun clearGroup(groupKey: String) {
            globalGroups.remove(groupKey)
        }

        fun clearAll() {
            globalGroups.clear()
        }
    }
}