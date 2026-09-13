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
    private var lastText: String? = null
    private var lastWidth: Float = -1f
    private var lastTextScale: Float = -1f
    private var memoizedHeight: Float = 0f

    override fun getHeightImpl(component: UIComponent): Float {
        val textComponent = (component as? UIWrappedText) ?: throw IllegalStateException("TextWrappingConstraint can only be used in UIWrappedText components")
        val text = textComponent.getText()
        val width = textComponent.getWidth()
        val scale = textComponent.getTextScale()

        if (text == lastText && width == lastWidth && scale == lastTextScale) {
            return memoizedHeight
        }

        val lines = getStringSplitToWidth(text, width, scale)
        val calculated = lines.size * 9 * scale

        lastText = text
        lastWidth = width
        lastTextScale = scale
        memoizedHeight = calculated

        return calculated
    }

    override fun visitImpl(
        visitor: ConstraintVisitor,
        type: ConstraintType
    ) {
        when (type) {
            ConstraintType.HEIGHT -> {
                visitor.visitSelf(ConstraintType.WIDTH)
                visitor.visitSelf(ConstraintType.TEXT_SCALE)
            }
            else -> throw IllegalArgumentException(type.prettyName)
        }
    }
}