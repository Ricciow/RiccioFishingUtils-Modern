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
import gg.essential.elementa.constraints.SizeConstraint
import gg.essential.elementa.constraints.resolution.ConstraintVisitor

/**
 * An enhanced FillConstraint that takes into account the component's position
 * (including SiblingConstraint distancing, offsets, and margins) as well as
 * any siblings after this component.
 */
class FillConstraint @JvmOverloads constructor(private val useSiblings: Boolean = true) : SizeConstraint {
    override var cachedValue = 0f
    override var recalculate = true
    override var constrainTo: UIComponent? = null

    override fun getWidthImpl(component: UIComponent): Float {
        val target = constrainTo ?: component.parent
        val children = target.children.filter { !it.isFloating }
        val indexInParent = children.indexOf(component)

        val spaceRight = if (useSiblings && indexInParent != -1 && indexInParent < children.size - 1) {
            children.subList(indexInParent + 1, children.size).sumOf {
                val w = it.getWidth()
                val spacing = (it.constraints.x as? SiblingConstraint)?.padding ?: 0f
                (w + spacing).toDouble()
            }.toFloat()
        } else {
            0f
        }

        return (target.getRight() - component.getLeft() - spaceRight).coerceAtLeast(0f)
    }

    override fun getHeightImpl(component: UIComponent): Float {
        val target = constrainTo ?: component.parent
        val children = target.children.filter { !it.isFloating }
        val indexInParent = children.indexOf(component)

        val spaceBelow = if (useSiblings && indexInParent != -1 && indexInParent < children.size - 1) {
            children.subList(indexInParent + 1, children.size).sumOf {
                val h = it.getHeight()
                val spacing = (it.constraints.y as? SiblingConstraint)?.padding ?: 0f
                (h + spacing).toDouble()
            }.toFloat()
        } else {
            0f
        }

        return (target.getBottom() - component.getTop() - spaceBelow).coerceAtLeast(0f)
    }

    override fun getRadiusImpl(component: UIComponent): Float {
        val target = constrainTo ?: component.parent
        return (target.getRadius() - component.getLeft()) / 2f
    }

    override fun visitImpl(visitor: ConstraintVisitor, type: ConstraintType) {
        val target = constrainTo ?: visitor.component.parent

        when (type) {
            ConstraintType.WIDTH -> {
                visitor.visitParent(ConstraintType.WIDTH)
                visitor.visitParent(ConstraintType.X)
                visitor.visitSelf(ConstraintType.X)

                if (useSiblings) {
                    val indexInParent = target.children.indexOf(visitor.component)
                    if (indexInParent != -1) {
                        for (i in (indexInParent + 1) until target.children.size) {
                            visitor.visitSibling(ConstraintType.WIDTH, i)
                        }
                    }
                }
            }
            ConstraintType.HEIGHT -> {
                visitor.visitParent(ConstraintType.HEIGHT)
                visitor.visitParent(ConstraintType.Y)
                visitor.visitSelf(ConstraintType.Y)

                if (useSiblings) {
                    val indexInParent = target.children.indexOf(visitor.component)
                    if (indexInParent != -1) {
                        for (i in (indexInParent + 1) until target.children.size) {
                            visitor.visitSibling(ConstraintType.HEIGHT, i)
                        }
                    }
                }
            }
            ConstraintType.RADIUS -> {
                visitor.visitParent(ConstraintType.RADIUS)
                visitor.visitParent(ConstraintType.Y)
                visitor.visitSelf(ConstraintType.Y)
            }
            else -> throw IllegalArgumentException(type.prettyName)
        }
    }
}
