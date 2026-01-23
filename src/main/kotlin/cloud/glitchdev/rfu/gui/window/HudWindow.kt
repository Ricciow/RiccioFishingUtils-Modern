package cloud.glitchdev.rfu.gui.window

import cloud.glitchdev.rfu.events.managers.HudRenderEvents.registerHudRenderEvent
import cloud.glitchdev.rfu.events.managers.ShutdownEvents.registerShutdownEvent
import cloud.glitchdev.rfu.gui.UIScheme
import cloud.glitchdev.rfu.gui.hud.AbstractHudElement
import cloud.glitchdev.rfu.manager.hud.HudManager
import cloud.glitchdev.rfu.utils.gui.Gui
import cloud.glitchdev.rfu.utils.gui.setHidden
import gg.essential.elementa.components.UIBlock
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.constraints.RelativeWindowConstraint
import gg.essential.elementa.dsl.childOf
import gg.essential.elementa.dsl.constrain
import gg.essential.elementa.dsl.toConstraint

object HudWindow : BaseWindow(false) {
    val backgroundColor = UIScheme.darkBackground.toConstraint()
    lateinit var background : UIBlock
    var isEditingOpen = false
    val hudElements : MutableList<AbstractHudElement> = mutableListOf()

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

    override fun onClose() {
        isEditingOpen = false
        updateState()
        for(element in hudElements) {
            element.closeEdit()
            HudManager.updateElementConfig(element)
        }
        super.onClose()
    }

    fun updateState() {
        background.setHidden(!isEditingOpen)
    }

    fun create() {
        background = UIBlock(backgroundColor).constrain {
            x = CenterConstraint()
            y = CenterConstraint()
            width = RelativeWindowConstraint(1f)
            height = RelativeWindowConstraint(1f)
        } childOf window

        background.hide()
    }

    fun registerHudElement(element: AbstractHudElement) {
        element childOf window
        hudElements.add(element)
        val elementData = HudManager.getElementConfig(element)

        element.apply {
            currentX = elementData.x
            currentY = elementData.y
            enabled = elementData.enabled
            scale = elementData.scale
        }
        element.updateState()
    }
}