package cloud.glitchdev.rfu.gui.components.elementa

import gg.essential.elementa.UIComponent
import gg.essential.elementa.components.UIWrappedText
import gg.essential.elementa.constraints.ConstraintType
import gg.essential.elementa.constraints.HeightConstraint
import gg.essential.elementa.constraints.resolution.ConstraintVisitor
import gg.essential.universal.UGraphics
import kotlin.math.ceil

class TextWrappingConstraint(
    override var cachedValue: Float = 0f,
    override var recalculate: Boolean = true,
    override var constrainTo: UIComponent? = null,
) : HeightConstraint {
    override fun getHeightImpl(component: UIComponent): Float {
        val text = (component as? UIWrappedText)?.getText() ?: throw IllegalStateException("TextWrappingConstraint can only be used in UIWrappedText components")
        return ceil(UGraphics.getStringWidth(text) / component.getWidth()) * 9
    }

    override fun visitImpl(
        visitor: ConstraintVisitor,
        type: ConstraintType
    ) {
        when (type) {
            ConstraintType.HEIGHT -> visitor.visitSelf(ConstraintType.HEIGHT)
            else -> throw IllegalArgumentException(type.prettyName)
        }
    }

}