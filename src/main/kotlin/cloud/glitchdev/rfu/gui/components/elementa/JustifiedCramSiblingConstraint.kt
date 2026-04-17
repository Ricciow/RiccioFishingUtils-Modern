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

    private fun getLines(parent: UIComponent): List<List<UIComponent>> {
        val lines = mutableListOf<List<UIComponent>>()
        var currentLine = mutableListOf<UIComponent>()
        var currentX = parent.getLeft()

        for (child in parent.children) {
            val childWidth = child.getWidth()

            // REMOVED basePadding from the wrap calculation.
            // Elements will now stay on the same line until they literally touch (padding = 0).
            if (currentLine.isNotEmpty() && currentX + childWidth > parent.getRight() + precisionAdjustmentFactor) {
                lines.add(currentLine)
                currentLine = mutableListOf(child)
                currentX = parent.getLeft() + childWidth
            } else {
                currentLine.add(child)
                currentX += childWidth
            }
        }

        if (currentLine.isNotEmpty()) {
            lines.add(currentLine)
        }
        return lines
    }

    override fun getXPositionImpl(component: UIComponent): Float {
        val parent = component.parent
        val index = parent.children.indexOf(component)

        if (index == 0) return parent.getLeft()

        val lines = getLines(parent)
        val lineIndex = lines.indexOfFirst { it.contains(component) }

        if (lineIndex == -1) return parent.getLeft()

        val line = lines[lineIndex]
        val indexInLine = line.indexOf(component)

        if (indexInLine == 0) {
            return parent.getLeft()
        }

        val totalComponentsWidth = line.sumOf { it.getWidth().toDouble() }.toFloat()
        val availableSpace = parent.getWidth() - totalComponentsWidth
        val dynamicPadding = availableSpace / (line.size - 1)

        // The core logic change: use basePadding, UNLESS the available spacing is smaller
        val actualPadding = min(basePadding, dynamicPadding)

        var x = parent.getLeft()
        for (i in 0 until indexInLine) {
            x += line[i].getWidth() + actualPadding
        }
        return x
    }

    override fun getYPositionImpl(component: UIComponent): Float {
        val parent = component.parent
        val index = parent.children.indexOf(component)

        if (index == 0) return parent.getTop()

        val lines = getLines(parent)
        val lineIndex = lines.indexOfFirst { it.contains(component) }

        if (lineIndex == -1) return parent.getTop()

        val line = lines[lineIndex]

        if (line.first() == component && lineIndex > 0) {
            val sibling = parent.children[index - 1]
            return getLowestPoint(sibling, parent, index) + basePadding
        }

        return line.first().getTop()
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

        val lines = getLines(parent)
        val lineIndex = lines.indexOfFirst { it.contains(component) }
        if (lineIndex == -1) return 0f

        val line = lines[lineIndex]
        if (line.first() == component) return 0f

        val totalComponentsWidth = line.sumOf { it.getWidth().toDouble() }.toFloat()
        val availableSpace = parent.getWidth() - totalComponentsWidth
        val dynamicPadding = availableSpace / (line.size - 1)

        return min(basePadding, dynamicPadding)
    }

    override fun getVerticalPadding(component: UIComponent): Float {
        val parent = component.parent
        val index = parent.children.indexOf(component)
        if (index == 0) return 0f

        val lines = getLines(parent)
        val lineIndex = lines.indexOfFirst { it.contains(component) }

        if (lineIndex > 0 && lines[lineIndex].first() == component) {
            return basePadding
        }

        return 0f
    }

    private companion object {
        private const val precisionAdjustmentFactor = 0.01f
    }
}