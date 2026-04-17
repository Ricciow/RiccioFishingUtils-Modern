package cloud.glitchdev.rfu.gui.components.elementa

import gg.essential.elementa.UIComponent
import gg.essential.elementa.constraints.ConstraintType
import gg.essential.elementa.constraints.MasterConstraint
import gg.essential.elementa.constraints.resolution.ConstraintVisitor
import gg.essential.elementa.state.BasicState
import gg.essential.elementa.state.MappedState
import gg.essential.elementa.state.State

/**
 * Sets this component's X/Y position to a constant number of pixels,
 * using the center of the component as the anchor point instead of its edges.
 */
class CenteredPixelConstraint @JvmOverloads constructor(
    value: State<Float>,
    alignOpposite: State<Boolean> = BasicState(false),
    alignOutside: State<Boolean> = BasicState(false)
) : MasterConstraint {
    @JvmOverloads constructor(
        value: Float,
        alignOpposite: Boolean = false,
        alignOutside: Boolean = false
    ) : this(BasicState(value), BasicState(alignOpposite), BasicState(alignOutside))

    override var cachedValue = 0f
    override var recalculate = true
    override var constrainTo: UIComponent? = null

    private val valueState: MappedState<Float, Float> = value.map { it }
    private val alignOppositeState: MappedState<Boolean, Boolean> = alignOpposite.map { it }
    private val alignOutsideState: MappedState<Boolean, Boolean> = alignOutside.map { it }

    var value: Float
        get() = valueState.get()
        set(value) { valueState.set(value) }
    var alignOpposite: Boolean
        get() = alignOppositeState.get()
        set(value) { alignOppositeState.set(value) }
    var alignOutside: Boolean
        get() = alignOutsideState.get()
        set(value) { alignOutsideState.set(value) }

    fun bindValue(newState: State<Float>) = apply {
        valueState.rebind(newState)
    }

    fun bindAlignOpposite(newState: State<Boolean>) = apply {
        alignOppositeState.rebind(newState)
    }

    fun bindAlignOutside(newState: State<Boolean>) = apply {
        alignOutsideState.rebind(newState)
    }

    override fun getXPositionImpl(component: UIComponent): Float {
        val target = constrainTo ?: try { component.parent } catch (_: Exception) { null }
        val value = this.valueState.get()
        val halfWidth = component.getWidth() / 2f

        if (target == null) return value - halfWidth

        return if (alignOppositeState.get()) {
            if (alignOutsideState.get()) {
                target.getRight() + value - halfWidth
            } else {
                target.getRight() - value - halfWidth
            }
        } else {
            if (alignOutsideState.get()) {
                target.getLeft() - value - halfWidth
            } else {
                target.getLeft() + value - halfWidth
            }
        }
    }

    override fun getYPositionImpl(component: UIComponent): Float {
        val target = constrainTo ?: try { component.parent } catch (_: Exception) { null }
        val value = this.valueState.get()
        val halfHeight = component.getHeight() / 2f

        if (target == null) return value - halfHeight

        return if (alignOppositeState.get()) {
            if (alignOutsideState.get()) {
                target.getBottom() + value - halfHeight
            } else {
                target.getBottom() - value - halfHeight
            }
        } else {
            if (alignOutsideState.get()) {
                target.getTop() - value - halfHeight
            } else {
                target.getTop() + value - halfHeight
            }
        }
    }

    override fun getWidthImpl(component: UIComponent): Float {
        return valueState.get()
    }

    override fun getHeightImpl(component: UIComponent): Float {
        return valueState.get()
    }

    override fun getRadiusImpl(component: UIComponent): Float {
        return valueState.get()
    }

    override fun visitImpl(visitor: ConstraintVisitor, type: ConstraintType) {
        when (type) {
            ConstraintType.X -> {
                visitor.visitParent(ConstraintType.X)
                visitor.visitSelf(ConstraintType.WIDTH) // Unconditionally required for the center offset
                if (alignOppositeState.get()) {
                    visitor.visitParent(ConstraintType.WIDTH)
                }
            }
            ConstraintType.Y -> {
                visitor.visitParent(ConstraintType.Y)
                visitor.visitSelf(ConstraintType.HEIGHT) // Unconditionally required for the center offset
                if (alignOppositeState.get()) {
                    visitor.visitParent(ConstraintType.HEIGHT)
                }
            }
            ConstraintType.WIDTH,
            ConstraintType.HEIGHT,
            ConstraintType.RADIUS,
            ConstraintType.TEXT_SCALE -> {}
            else -> throw IllegalArgumentException(type.prettyName)
        }
    }
}