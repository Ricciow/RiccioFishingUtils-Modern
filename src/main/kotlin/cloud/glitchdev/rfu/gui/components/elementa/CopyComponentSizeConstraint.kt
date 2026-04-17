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
            // Evaluates the constraint of the target component directly
            targetComponent.constraints.width.getWidthImpl(targetComponent)
        } catch (e: Exception) {
            fallbackConstraint.getWidthImpl(component)
        }
    }

    override fun getHeightImpl(component: UIComponent): Float {
        return try {
            targetComponent.constraints.height.getHeightImpl(targetComponent)
        } catch (e: Exception) {
            fallbackConstraint.getHeightImpl(component)
        }
    }

    override fun getRadiusImpl(component: UIComponent): Float {
        return try {
            targetComponent.constraints.radius.getRadiusImpl(targetComponent)
        } catch (e: Exception) {
            fallbackConstraint.getRadiusImpl(component)
        }
    }

    override fun visitImpl(visitor: ConstraintVisitor, type: ConstraintType) {
        fallbackConstraint.visitImpl(visitor, type)
    }
}