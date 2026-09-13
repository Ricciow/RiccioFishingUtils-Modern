/*
 * Copyright (C) 2026 EssentialGG (Elementa)
 * Copyright (C) 2026 Riccio (Modifications)
 *
 * This file is part of Elementa (modified).
 *
 * Elementa is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Elementa is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package cloud.glitchdev.rfu.gui.components.elementa

import gg.essential.elementa.UIComponent
import gg.essential.elementa.constraints.ConstraintType
import gg.essential.elementa.constraints.SiblingConstraint
import gg.essential.elementa.constraints.resolution.ConstraintVisitor
import java.lang.UnsupportedOperationException
import kotlin.math.min

/**
 * Left-aligns elements using [basePadding] as a maximum gap.
 * If the elements exceed the parent width, it squishes the padding (making it smaller)
 * to fit them evenly on one line. It only wraps when the padding reaches 0.
 */
class JustifiedCramSiblingConstraint(private val basePadding: Float = 0f) : SiblingConstraint(basePadding) {
    override var cachedValue = 0f
    override var recalculate = true
    override var constrainTo: UIComponent? = null

    private data class ChildPos(
        val x: Float,
        val y: Float,
        val horizontalPadding: Float,
        val verticalPadding: Float
    )

    private class ParentLayout(
        val parentLeft: Float,
        val parentTop: Float,
        val parentWidth: Float,
        val childrenCount: Int,
        val firstChild: UIComponent?,
        val lastChild: UIComponent?,
        val positions: Map<UIComponent, ChildPos>
    )

    private fun getLayout(parent: UIComponent): ParentLayout {
        val cached = layoutCache[parent]
        val children = parent.children
        val parentLeft = parent.getLeft()
        val parentTop = parent.getTop()
        val parentWidth = parent.getWidth()
        val firstChild = children.firstOrNull()
        val lastChild = children.lastOrNull()

        if (cached != null &&
            cached.parentLeft == parentLeft &&
            cached.parentTop == parentTop &&
            cached.parentWidth == parentWidth &&
            cached.childrenCount == children.size &&
            cached.firstChild === firstChild &&
            cached.lastChild === lastChild
        ) {
            return cached
        }

        val lines = mutableListOf<List<UIComponent>>()
        var currentLine = mutableListOf<UIComponent>()
        var currentX = parentLeft

        for (child in children) {
            val childWidth = child.getWidth()

            if (currentLine.isNotEmpty() && currentX + childWidth > parent.getRight() + precisionAdjustmentFactor) {
                lines.add(currentLine)
                currentLine = mutableListOf(child)
                currentX = parentLeft + childWidth
            } else {
                currentLine.add(child)
                currentX += childWidth
            }
        }

        if (currentLine.isNotEmpty()) {
            lines.add(currentLine)
        }

        val positions = HashMap<UIComponent, ChildPos>(children.size)
        var currentLineTop = parentTop

        for ((lineIndex, line) in lines.withIndex()) {
            var totalComponentsWidth = 0.0
            for (child in line) {
                totalComponentsWidth += child.getWidth().toDouble()
            }
            val availableSpace = parentWidth - totalComponentsWidth.toFloat()
            val dynamicPadding = if (line.size > 1) availableSpace / (line.size - 1) else 0f
            val actualPadding = min(basePadding, dynamicPadding)

            var childX = parentLeft
            var maxChildHeight = 0f

            for ((indexInLine, child) in line.withIndex()) {
                val hPadding = if (indexInLine == 0) 0f else actualPadding
                val vPadding = if (lineIndex > 0 && indexInLine == 0) basePadding else 0f
                positions[child] = ChildPos(childX, currentLineTop, hPadding, vPadding)

                val childWidth = child.getWidth()
                val childHeight = child.getHeight()
                if (childHeight > maxChildHeight) {
                    maxChildHeight = childHeight
                }
                childX += childWidth + actualPadding
            }

            currentLineTop += maxChildHeight + basePadding
        }

        val newLayout = ParentLayout(
            parentLeft,
            parentTop,
            parentWidth,
            children.size,
            firstChild,
            lastChild,
            positions
        )
        layoutCache[parent] = newLayout
        return newLayout
    }

    override fun getXPositionImpl(component: UIComponent): Float {
        val parent = component.parent
        val index = parent.children.indexOf(component)
        if (index == 0) return parent.getLeft()

        val layout = getLayout(parent)
        return layout.positions[component]?.x ?: parent.getLeft()
    }

    override fun getYPositionImpl(component: UIComponent): Float {
        val parent = component.parent
        val index = parent.children.indexOf(component)
        if (index == 0) return parent.getTop()

        val layout = getLayout(parent)
        return layout.positions[component]?.y ?: parent.getTop()
    }

    override fun to(component: UIComponent) = apply {
        throw UnsupportedOperationException("Constraint.to(UIComponent) is not available in this context!")
    }

    override fun visitImpl(visitor: ConstraintVisitor, type: ConstraintType) {
        val indexInParent = visitor.component.parent.children.indexOf(visitor.component)

        when (type) {
            ConstraintType.X, ConstraintType.Y -> {
                if (indexInParent <= 0) {
                    visitor.visitParent(type)
                    return
                }

                visitor.visitSibling(ConstraintType.X, indexInParent - 1)
                visitor.visitSibling(ConstraintType.Y, indexInParent - 1)
                visitor.visitSibling(ConstraintType.WIDTH, indexInParent - 1)
                visitor.visitSibling(ConstraintType.HEIGHT, indexInParent - 1)

                visitor.visitSelf(ConstraintType.WIDTH)
                visitor.visitParent(ConstraintType.X)
                visitor.visitParent(ConstraintType.WIDTH)
            }
            else -> throw IllegalArgumentException(type.prettyName)
        }
    }

    override fun getHorizontalPadding(component: UIComponent): Float {
        val parent = component.parent
        val index = parent.children.indexOf(component)
        if (index == 0) return 0f

        val layout = getLayout(parent)
        return layout.positions[component]?.horizontalPadding ?: 0f
    }

    override fun getVerticalPadding(component: UIComponent): Float {
        val parent = component.parent
        val index = parent.children.indexOf(component)
        if (index == 0) return 0f

        val layout = getLayout(parent)
        return layout.positions[component]?.verticalPadding ?: 0f
    }

    private companion object {
        private const val precisionAdjustmentFactor = 0.01f
        private val layoutCache = java.util.WeakHashMap<UIComponent, ParentLayout>()
    }
}