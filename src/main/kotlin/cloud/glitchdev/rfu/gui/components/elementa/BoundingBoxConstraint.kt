package cloud.glitchdev.rfu.gui.components.elementa

import gg.essential.elementa.UIComponent
import gg.essential.elementa.constraints.*
import gg.essential.elementa.constraints.resolution.ConstraintVisitor

/**
 * Constraints the width/height to the actual elements inside it
 * if the elements use relative constraints it climbs up the
 * component tree until it finds a suitable relative parent.
 */
class BoundingBoxConstraint : SizeConstraint {
    override var cachedValue = 0f
    override var recalculate = true
    override var constrainTo: UIComponent? = null

    /**
     * Checks if a given constraint relies on its children for its size.
     * We recursively check wrappers (like AdditiveConstraint) in case you do
     * something like `ChildBasedSizeConstraint() + 10.pixels`.
     */
    private fun isChildDependent(constraint: Any?): Boolean {
        if (constraint == null) return false

        if (constraint is BoundingBoxConstraint) return true
        if (constraint is ChildBasedSizeConstraint) return true
        if (constraint is ChildBasedMaxSizeConstraint) return true
        if (constraint is GroupMaxSizeConstraint) return true

        return when (constraint) {
            is AdditiveConstraint -> isChildDependent(constraint.constraint1) || isChildDependent(constraint.constraint2)
            is SubtractiveConstraint -> isChildDependent(constraint.constraint1) || isChildDependent(constraint.constraint2)
            is MinConstraint -> isChildDependent(constraint.first) || isChildDependent(constraint.second)
            is MaxConstraint -> isChildDependent(constraint.first) || isChildDependent(constraint.second)
            is ScaleConstraint -> isChildDependent(constraint.constraint)
            else -> false
        }
    }

    /**
     * Climbs up the UI tree to find the first ancestor that does NOT shrink-wrap.
     * This guarantees we find a component with a concrete, calculable size.
     */
    private fun findSafeAncestor(startComponent: UIComponent, isWidth: Boolean): UIComponent? {
        var current: UIComponent? = startComponent.parent

        while (current != null) {
            val axisConstraint = if (isWidth) current.constraints.width else current.constraints.height

            if (!isChildDependent(axisConstraint)) {
                return current // We found a safe container!
            }
            current = current.parent
        }

        return startComponent.parent
    }

    private fun redirectToAncestor(constraint: Any?, safeAncestor: UIComponent) {
        when (constraint) {
            is RelativeConstraint -> {
                if (constraint.constrainTo == null) constraint.constrainTo = safeAncestor
            }
            is FillConstraint -> {
                if (constraint.constrainTo == null) constraint.constrainTo = safeAncestor
            }
            is CenterConstraint -> {
                if (constraint.constrainTo == null) constraint.constrainTo = safeAncestor
            }
            is MinConstraint -> {
                redirectToAncestor(constraint.first, safeAncestor)
                redirectToAncestor(constraint.second, safeAncestor)
            }
            is MaxConstraint -> {
                redirectToAncestor(constraint.first, safeAncestor)
                redirectToAncestor(constraint.second, safeAncestor)
            }
            is AdditiveConstraint -> {
                redirectToAncestor(constraint.constraint1, safeAncestor)
                redirectToAncestor(constraint.constraint2, safeAncestor)
            }
            is ScaleConstraint -> {
                redirectToAncestor(constraint.constraint, safeAncestor)
            }
        }
    }

    private fun patchChildrenConstraints(target: UIComponent, isWidth: Boolean) {
        val safeAncestor = findSafeAncestor(target, isWidth) ?: return

        target.children.forEach { child ->
            if (isWidth) {
                redirectToAncestor(child.constraints.width, safeAncestor)
                redirectToAncestor(child.constraints.x, safeAncestor)
            } else {
                redirectToAncestor(child.constraints.height, safeAncestor)
                redirectToAncestor(child.constraints.y, safeAncestor)
            }
        }
    }

    override fun getWidthImpl(component: UIComponent): Float {
        val target = constrainTo ?: component
        if (target.children.isEmpty()) return 0f

        patchChildrenConstraints(target, isWidth = true)

        val rightMost = target.children.maxOfOrNull {
            it.getRight()
        } ?: target.getLeft()

        return rightMost - target.getLeft()
    }

    override fun getHeightImpl(component: UIComponent): Float {
        val target = constrainTo ?: component
        if (target.children.isEmpty()) return 0f

        patchChildrenConstraints(target, isWidth = false)

        val bottomMost = target.children.maxOfOrNull {
            it.getBottom()
        } ?: target.getTop()

        return bottomMost - target.getTop()
    }

    override fun getRadiusImpl(component: UIComponent): Float {
        return getHeightImpl(component) / 2f
    }

    override fun visitImpl(visitor: ConstraintVisitor, type: ConstraintType) {
        val component = visitor.component

        if (type == ConstraintType.WIDTH || type == ConstraintType.X) {
            patchChildrenConstraints(component, isWidth = true)
        } else if (type == ConstraintType.HEIGHT || type == ConstraintType.Y || type == ConstraintType.RADIUS) {
            patchChildrenConstraints(component, isWidth = false)
        }

        when (type) {
            ConstraintType.WIDTH -> {
                visitor.visitChildren(ConstraintType.X)
                visitor.visitChildren(ConstraintType.WIDTH)
            }
            ConstraintType.HEIGHT -> {
                visitor.visitChildren(ConstraintType.Y)
                visitor.visitChildren(ConstraintType.HEIGHT)
            }
            ConstraintType.RADIUS -> visitor.visitChildren(ConstraintType.HEIGHT)
            else -> throw IllegalArgumentException(type.prettyName)
        }
    }
}