package cloud.glitchdev.rfu.gui.window

import cloud.glitchdev.rfu.events.managers.HudRenderEvents.registerHudRenderEvent
import cloud.glitchdev.rfu.events.managers.ShutdownEvents.registerShutdownEvent
import cloud.glitchdev.rfu.gui.UIScheme
import cloud.glitchdev.rfu.gui.hud.AbstractHudElement
import cloud.glitchdev.rfu.manager.hud.HudManager
import cloud.glitchdev.rfu.utils.gui.Gui
import cloud.glitchdev.rfu.utils.gui.setHidden
import gg.essential.elementa.components.UIBlock
import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.components.UIWrappedText
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.constraints.RelativeWindowConstraint
import gg.essential.elementa.constraints.SiblingConstraint
import gg.essential.elementa.dsl.childOf
import gg.essential.elementa.dsl.constrain
import gg.essential.elementa.dsl.pixels
import gg.essential.elementa.dsl.toConstraint
import java.awt.Color

object HudWindow : BaseWindow(false) {
    val backgroundColor = UIScheme.darkBackground.toConstraint()
    lateinit var background : UIBlock
    var isEditingOpen = false
    val hudElements : MutableList<AbstractHudElement> = mutableListOf()

    lateinit var vSnapLine: UIContainer
    lateinit var hSnapLine: UIContainer
    lateinit var infoText : UIWrappedText

    init {
        create()

        registerHudRenderEvent { context, ticks ->
            if(!isEditingOpen) {
                render(context, 0, 0, ticks)
            }
        }

        registerShutdownEvent(0) {
            for(element in hudElements) {
                HudManager.updateElementConfig(element)
            }
        }
    }

    fun openEditingGui() {
        isEditingOpen = true
        updateState()
        for(element in hudElements) {
            element.openEdit()
        }
        Gui.openGui(this)
    }

    override fun onWindowClose() {
        isEditingOpen = false
        updateState()
        for(element in hudElements) {
            element.closeEdit()
            HudManager.updateElementConfig(element)
        }
    }

    fun updateState() {
        background.setHidden(!isEditingOpen)
    }

    fun showSnapLines(x: Float?, y: Float?) {
        vSnapLine.constrain {
            this.x = (if (x != null) x - 0.5f else -1000f).pixels()
        }

        hSnapLine.constrain {
            this.y = (if (y != null) y -  0.5f else -1000f).pixels()
        }
    }

    fun setInfotextState(state : Boolean) {
        infoText.setHidden(!state)
    }

    fun create() {
        background = UIBlock(backgroundColor).constrain {
            x = CenterConstraint()
            y = CenterConstraint()
            width = RelativeWindowConstraint(1f)
            height = RelativeWindowConstraint(1f)
        } childOf window

        infoText = UIWrappedText(
            text = """Hold Ctrl/Shift to disable snapping
                     |Scroll up/down to resize, hold Ctrl/Shift for more precision""".trimMargin(),
            shadow = true,
            centered = true
        ).constrain {
            x = CenterConstraint()
            y = CenterConstraint()
        } childOf background

        vSnapLine = UIContainer().constrain {
            x = (-1000).pixels()
            y = 0.pixels()
            width = 1.pixels()
            height = RelativeWindowConstraint(1f)
        } childOf window
        vSnapLine.isFloating = true

        hSnapLine = UIContainer().constrain {
            x = 0.pixels()
            y = (-1000).pixels()
            width = RelativeWindowConstraint(1f)
            height = 1.pixels()
        } childOf window
        hSnapLine.isFloating = true

        // Create dots for lines
        for (i in 0 until 100) {
            UIBlock(Color.WHITE).constrain {
                x = CenterConstraint()
                y = SiblingConstraint(5f)
                width = 1.pixels()
                height = 5.pixels()
            } childOf vSnapLine

            UIBlock(Color.WHITE).constrain {
                x = SiblingConstraint(5f)
                y = CenterConstraint()
                width = 5.pixels()
                height = 1.pixels()
            } childOf hSnapLine
        }

        background.hide()
    }

    fun registerHudElement(element: AbstractHudElement) {
        element childOf window
        hudElements.add(element)
        val elementData = HudManager.getElementConfig(element)

        element.apply {
            currentX = elementData.x
            currentY = elementData.y
            scale = elementData.scale
        }
        element.updateState()
    }
}