package gg.essential.elementa.example

import gg.essential.elementa.ElementaVersion
import gg.essential.elementa.WindowScreen
import gg.essential.elementa.components.*
import gg.essential.elementa.components.input.UITextInput
import gg.essential.elementa.constraints.*
import gg.essential.elementa.constraints.animation.Animations
import gg.essential.elementa.dsl.*
import gg.essential.elementa.effects.ScissorEffect
import java.awt.Color

/**
 * ExampleGui is a fully fleshed example of a lot of Elementa's features
 * and how to effectively use them. This example is a "sticky note pad"
 * where users can create, delete, move, and write on little sticky notes.
 *
 * The example won't look particularly pretty, but that is up to the programmer
 * to design their GUIs how they wish.
 */
class ExampleGui : WindowScreen(ElementaVersion.V2) {

    init {

        val createNoteButton = UIBlock(Color(207, 207, 196)).constrain {
            x = 2.pixels()
            y = 2.pixels()

            width = ChildBasedSizeConstraint() + 4.pixels()

            height = ChildBasedMaxSizeConstraint() + 4.pixels()
        }.onMouseClick {

            StickyNote() childOf window
        }.onMouseEnter {

            this.animate {
                setColorAnimation(
                    Animations.OUT_EXP,
                    0.5f,
                    Color(120, 120, 100).toConstraint(),
                    0f
                )
            }
        }.onMouseLeave {

            animate {
                setColorAnimation(
                    Animations.OUT_EXP,
                    0.5f,
                    Color(207, 207, 196).toConstraint()
                )
            }
        } childOf window


        UIText("Create notes!", shadow = false).constrain {
            x = 2.pixels()
            y = CenterConstraint()


            textScale = 2.pixels()

            color = Color.GREEN.darker().toConstraint()
        } childOf createNoteButton

    }

    class StickyNote : UIBlock(Color.BLACK) {
        private var isDragging: Boolean = false
        private var dragOffset: Pair<Float, Float> = 0f to 0f

        private val textArea: UITextInput

        init {
            constrain {
                x = CenterConstraint()
                y = CenterConstraint()

                width = 150.pixels()
                height = 100.pixels()
            }

            onMouseClick {
                parent.removeChild(this)
                parent.addChild(this)
            }

            val topBar = UIBlock(Color.YELLOW).constrain {
                x = 1.pixel()
                y = 1.pixel()

                width = 100.percent() - 2.pixels()

                height = 24.pixels()
            }.onMouseClick { event ->
                isDragging = true

                dragOffset = event.absoluteX to event.absoluteY
            }.onMouseRelease {

                isDragging = false
            }.onMouseDrag { mouseX, mouseY, _ ->

                if (!isDragging) return@onMouseDrag

                val absoluteX = mouseX + getLeft()
                val absoluteY = mouseY + getTop()

                val deltaX = absoluteX - dragOffset.first
                val deltaY = absoluteY - dragOffset.second

                dragOffset = absoluteX to absoluteY

                val newX = this@StickyNote.getLeft() + deltaX
                val newY = this@StickyNote.getTop() + deltaY

                this@StickyNote.setX(newX.pixels())
                this@StickyNote.setY(newY.pixels())
            } childOf this

            UIText("X", shadow = false).constrain {
                x = 4.pixels(alignOpposite = true)

                y = CenterConstraint()

                color = Color.BLACK.toConstraint()

                textScale = 2.pixels()
            }.onMouseEnter {
                animate {
                    setColorAnimation(Animations.OUT_EXP, 0.5f, Color.RED.toConstraint())
                }
            }.onMouseLeave {
                animate {
                    setColorAnimation(Animations.OUT_EXP, 0.5f, Color.BLACK.toConstraint())
                }
            }.onMouseClick { event ->
                this@StickyNote.parent.removeChild(this@StickyNote)

                event.stopPropagation()
            } childOf topBar

            val textHolder = UIBlock(Color(80, 80, 80)).constrain {
                x = 1.pixel()

                y = SiblingConstraint()

                width = RelativeConstraint(1f) - 2.pixels()

                height = FillConstraint()
            } childOf this

            textHolder effect ScissorEffect()

            textArea = (UITextInput(placeholder = "Enter your note...").constrain {
                x = 2.pixels()
                y = 2.pixels()
                height = FillConstraint() - 2.pixels()
            }.onMouseClick {
                grabWindowFocus()
            } childOf textHolder) as UITextInput
        }
    }
}