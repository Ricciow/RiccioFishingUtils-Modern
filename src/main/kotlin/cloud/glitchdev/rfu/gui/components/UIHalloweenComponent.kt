package cloud.glitchdev.rfu.gui.components

import cloud.glitchdev.rfu.utils.SeasonalEffects
import cloud.glitchdev.rfu.utils.gui.setHidden
import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.components.UIImage
import gg.essential.elementa.constraints.AspectConstraint
import gg.essential.elementa.constraints.PositionConstraint
import gg.essential.elementa.constraints.RelativeConstraint
import gg.essential.elementa.constraints.animation.Animations
import gg.essential.elementa.dsl.animate
import gg.essential.elementa.dsl.childOf
import gg.essential.elementa.dsl.constrain
import gg.essential.elementa.dsl.effect
import gg.essential.elementa.dsl.percent
import gg.essential.elementa.dsl.pixels
import gg.essential.elementa.effects.ScissorEffect
import kotlin.random.Random

class UIHalloweenComponent : UIContainer() {
    private data class Point(val x: Float, val y: Float)

    private val leftBats = UIImage.ofResourceCached("/assets/rfu/ui/halloween/bat_flock_left.png").constrain {
        width = RelativeConstraint(0.1f)
        height = AspectConstraint(0.66f)
    } childOf this

    private val rightBats = UIImage.ofResourceCached("/assets/rfu/ui/halloween/bat_flock_right.png").constrain {
        width = RelativeConstraint(0.1f)
        height = AspectConstraint(0.66f)
    } childOf this

    init {
        constrain {
            width = RelativeConstraint(1f)
            height = RelativeConstraint(1f)
        }
        effect(ScissorEffect())

        leftBats.hide(true)
        rightBats.hide(true)

        UIImage.ofResourceCached("/assets/rfu/ui/halloween/pumpkins.png").constrain {
            width = 10.percent
            height = AspectConstraint(0.75f)
            x = 1.percent
            y = 5.pixels(true)
        } childOf this
        UIImage.ofResourceCached("/assets/rfu/ui/halloween/bones.png").constrain {
            width = 10.percent
            height = AspectConstraint(0.75f)
            x = 89.percent
            y = 5.pixels(true)
        } childOf this
    }

    override fun afterInitialization() {
        super.afterInitialization()
        animateBats()
    }

    override fun isPointInside(x: Float, y: Float): Boolean = false

    private fun sidePoint(left: Boolean): Point =
        Point(if (left) 0f else 1f, Random.nextDouble(0.2, 0.7).toFloat())

    private fun relativePosition(fraction: Float): PositionConstraint = when (fraction) {
        0f -> 0.pixels(alignOutside = true)
        1f -> 0.pixels(alignOpposite = true, alignOutside = true)
        else -> RelativeConstraint(fraction)
    }

    private fun animateBats() {
        val start: Point
        val end: Point
        when (Random.nextInt(3)) {
            0 -> {
                start = sidePoint(true)
                end = sidePoint(false)
            }
            1 -> {
                start = sidePoint(false)
                end = sidePoint(true)
            }
            else -> {
                start = Point(Random.nextDouble(0.0, 1.0).toFloat(), 1f)
                end = when (Random.nextInt(3)) {
                    0 -> Point(Random.nextDouble(0.0, 1.0).toFloat(), 0f)
                    1 -> sidePoint(true)
                    else -> sidePoint(false)
                }
            }
        }
        val image = if (end.x < start.x) leftBats else rightBats
        image.constrain {
            x = relativePosition(start.x)
            y = relativePosition(start.y)
        }
        image.unhide()

        val duration = Random.nextInt(5, 11).toFloat()
        val delay = Random.nextInt(15, 31).toFloat()
        image.animate {
            setXAnimation(Animations.LINEAR, duration, relativePosition(end.x), delay)
            setYAnimation(Animations.LINEAR, duration, relativePosition(end.y), delay)
            onComplete {
                image.hide(true)
                animateBats()
            }
        }
    }

    fun update() {
        setHidden(!SeasonalEffects.halloweenActive)
    }
}
