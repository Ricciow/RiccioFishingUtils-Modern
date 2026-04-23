package cloud.glitchdev.rfu.gui.components.elementa

import gg.essential.elementa.UIComponent
import gg.essential.elementa.constraints.ConstraintType
import gg.essential.elementa.constraints.PaddingConstraint
import gg.essential.elementa.constraints.SizeConstraint
import gg.essential.elementa.constraints.resolution.ConstraintVisitor

class CramAwareMaxSizeConstraint : SizeConstraint {
    override var cachedValue = 0f
    override var recalculate = true
    override var constrainTo: UIComponent? = null

    override fun getWidthImpl(component: UIComponent): Float {
        val target = constrainTo ?: component
        if (target.children.isEmpty()) return 0f

        return target.children.maxOfOrNull {
            it.getWidth() + ((it.constraints.x as? PaddingConstraint)?.getHorizontalPadding(it) ?: 0f)
        } ?: 0f
    }

    override fun getHeightImpl(component: UIComponent): Float {
        val target = constrainTo ?: component
        if (target.children.isEmpty()) return 0f

        val lowestBottom = target.children.maxOfOrNull {
            it.getBottom() + ((it.constraints.y as? PaddingConstraint)?.getVerticalPadding(it) ?: 0f)
        } ?: target.getTop()

        return lowestBottom - target.getTop()
    }

    override fun getRadiusImpl(component: UIComponent): Float {
        return getHeightImpl(component) / 2f
    }

    override fun visitImpl(visitor: ConstraintVisitor, type: ConstraintType) {
        when (type) {
            ConstraintType.WIDTH -> visitor.visitChildren(ConstraintType.WIDTH)
            ConstraintType.HEIGHT -> {
                visitor.visitChildren(ConstraintType.Y)
                visitor.visitChildren(ConstraintType.HEIGHT)
            }
            ConstraintType.RADIUS -> visitor.visitChildren(ConstraintType.HEIGHT)
            else -> throw IllegalArgumentException(type.prettyName)
        }
    }
}