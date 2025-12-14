package cloud.glitchdev.rfu.gui.components.elementa

import gg.essential.elementa.UIComponent
import gg.essential.elementa.components.UIWrappedText
import gg.essential.elementa.constraints.ConstraintType
import gg.essential.elementa.constraints.HeightConstraint
import gg.essential.elementa.constraints.resolution.ConstraintVisitor
import gg.essential.elementa.utils.getStringSplitToWidth

class TextWrappingConstraint(
    override var cachedValue: Float = 0f,
    override var recalculate: Boolean = true,
    override var constrainTo: UIComponent? = null,
) : HeightConstraint {
    override fun getHeightImpl(component: UIComponent): Float {
        val textComponent = (component as? UIWrappedText) ?: throw IllegalStateException("TextWrappingConstraint can only be used in UIWrappedText components")
        val lines = getStringSplitToWidth(textComponent.getText(), textComponent.getWidth(), textComponent.getTextScale())
        return lines.size * 9 * textComponent.getTextScale()
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