package cloud.glitchdev.rfu.gui.components.elementa.group

import cloud.glitchdev.rfu.utils.gui.isDeepHidden
import gg.essential.elementa.UIComponent
import gg.essential.elementa.components.Window
import gg.essential.elementa.constraints.ConstraintType
import gg.essential.elementa.constraints.SizeConstraint
import gg.essential.elementa.constraints.resolution.ConstraintVisitor
import gg.essential.elementa.dsl.pixel

class GroupMaxSizeConstraint(
    val groupKey: String,
    val baseConstraint: SizeConstraint = 0.pixel
) : SizeConstraint {
    override var cachedValue = 0f
    override var recalculate: Boolean
        get() = true
        set(value) {}
    override var constrainTo: UIComponent? = null

    override fun getWidthImpl(component: UIComponent): Float = getMaxValue(component, ConstraintType.WIDTH)
    override fun getHeightImpl(component: UIComponent): Float = getMaxValue(component, ConstraintType.HEIGHT)
    override fun getRadiusImpl(component: UIComponent): Float = getMaxValue(component, ConstraintType.RADIUS)

    override fun visitImpl(visitor: ConstraintVisitor, type: ConstraintType) {
        baseConstraint.visitImpl(visitor, type)
    }

    private fun getMaxValue(component: UIComponent, type: ConstraintType): Float {
        val window = Window.ofOrNull(component) ?: return getBaseValue(component, type)
        val frameTime = window.animationTimeMs

        val group = GroupManager.getOrCreateGroup(groupKey)

        if (!component.isDeepHidden()) {
            group.register(this, component)
        }

        return group.getMaxValue(type, frameTime, this, component)
    }

    internal fun getBaseValue(component: UIComponent, type: ConstraintType): Float {
        return when (type) {
            ConstraintType.WIDTH -> baseConstraint.getWidthImpl(component)
            ConstraintType.HEIGHT -> baseConstraint.getHeightImpl(component)
            ConstraintType.RADIUS -> baseConstraint.getRadiusImpl(component)
            else -> 0f
        }
    }

    companion object {
        fun clearGroup(groupKey: String) = GroupManager.clearGroup(groupKey)
        fun clearAll() = GroupManager.clearAll()
    }
}
