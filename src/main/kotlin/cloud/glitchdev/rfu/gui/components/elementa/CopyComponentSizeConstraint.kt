package cloud.glitchdev.rfu.gui.components.elementa

import gg.essential.elementa.UIComponent
import gg.essential.elementa.constraints.ConstraintType
import gg.essential.elementa.constraints.SizeConstraint
import gg.essential.elementa.constraints.resolution.ConstraintVisitor
import gg.essential.elementa.dsl.pixel

class CopyComponentSizeConstraint(
    val targetComponent: UIComponent,
    val fallbackConstraint: SizeConstraint = 0.pixel
) : SizeConstraint {
    override var cachedValue = 0f
    override var recalculate = true
    override var constrainTo: UIComponent? = null

    override fun getWidthImpl(component: UIComponent): Float {
        return try {
            targetComponent.getWidth()
        } catch (e: Exception) {
            fallbackConstraint.getWidth(component)
        }
    }

    override fun getHeightImpl(component: UIComponent): Float {
        return try {
            targetComponent.getHeight()
        } catch (e: Exception) {
            fallbackConstraint.getHeight(component)
        }
    }

    override fun getRadiusImpl(component: UIComponent): Float {
        return try {
            targetComponent.getRadius()
        } catch (e: Exception) {
            fallbackConstraint.getRadius(component)
        }
    }

    override fun visitImpl(visitor: ConstraintVisitor, type: ConstraintType) {
        fallbackConstraint.visitImpl(visitor, type)
    }
}