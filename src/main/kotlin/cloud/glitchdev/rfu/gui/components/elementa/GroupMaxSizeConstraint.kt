package cloud.glitchdev.rfu.gui.components.elementa

import gg.essential.elementa.UIComponent
import gg.essential.elementa.constraints.ConstraintType
import gg.essential.elementa.constraints.SizeConstraint
import gg.essential.elementa.constraints.resolution.ConstraintVisitor
import gg.essential.elementa.dsl.pixel

class GroupMaxSizeConstraint(
    val groupKey: String,
    val baseConstraint: SizeConstraint = 0.pixel
) : SizeConstraint {
    override var cachedValue = 0f
    override var recalculate = true
    override var constrainTo: UIComponent? = null

    override fun getWidthImpl(component: UIComponent): Float {
        if (!component.hasParent) return baseConstraint.getWidthImpl(component)

        return component.parent.children.maxOfOrNull { sibling ->
            val siblingConstraint = sibling.constraints.width as? GroupMaxSizeConstraint

            if (siblingConstraint != null && siblingConstraint.groupKey == this.groupKey) {
                siblingConstraint.baseConstraint.getWidthImpl(sibling)
            } else {
                0f
            }
        } ?: 0f
    }

    override fun getHeightImpl(component: UIComponent): Float {
        if (!component.hasParent) return baseConstraint.getHeightImpl(component)

        return component.parent.children.maxOfOrNull { sibling ->
            val siblingConstraint = sibling.constraints.height as? GroupMaxSizeConstraint

            if (siblingConstraint != null && siblingConstraint.groupKey == this.groupKey) {
                siblingConstraint.baseConstraint.getHeightImpl(sibling)
            } else {
                0f
            }
        } ?: 0f
    }

    override fun getRadiusImpl(component: UIComponent): Float {
        if (!component.hasParent) return baseConstraint.getRadiusImpl(component)

        return component.parent.children.maxOfOrNull { sibling ->
            val siblingConstraint = sibling.constraints.radius as? GroupMaxSizeConstraint

            if (siblingConstraint != null && siblingConstraint.groupKey == this.groupKey) {
                siblingConstraint.baseConstraint.getRadiusImpl(sibling)
            } else {
                0f
            }
        } ?: 0f
    }

    override fun visitImpl(visitor: ConstraintVisitor, type: ConstraintType) {
        baseConstraint.visitImpl(visitor, type)
    }
}