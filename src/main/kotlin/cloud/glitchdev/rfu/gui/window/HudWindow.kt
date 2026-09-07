package cloud.glitchdev.rfu.gui.window

import cloud.glitchdev.rfu.constants.text.TextColor
import cloud.glitchdev.rfu.constants.text.TextStyle
import cloud.glitchdev.rfu.events.managers.ConnectionEvents.registerJoinEvent
import cloud.glitchdev.rfu.events.managers.HudRenderEvents.registerHudRenderEvent
import cloud.glitchdev.rfu.events.managers.ShutdownEvents.registerShutdownEvent
import cloud.glitchdev.rfu.events.managers.TickEvents.registerTickEvent
import cloud.glitchdev.rfu.gui.UIScheme
import cloud.glitchdev.rfu.gui.components.UIButton
import cloud.glitchdev.rfu.gui.hud.AbstractHudElement
import cloud.glitchdev.rfu.data.hud.DefaultHudManager
import cloud.glitchdev.rfu.data.hud.HudManager
import cloud.glitchdev.rfu.utils.Chat
import cloud.glitchdev.rfu.utils.TextUtils
import cloud.glitchdev.rfu.utils.gui.Gui
import cloud.glitchdev.rfu.utils.gui.isHidden
import cloud.glitchdev.rfu.utils.gui.setHidden
import gg.essential.elementa.components.UIBlock
import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.components.UIWrappedText
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.constraints.RelativeWindowConstraint
import gg.essential.elementa.constraints.SiblingConstraint
import gg.essential.elementa.dsl.childOf
import gg.essential.elementa.dsl.constrain
import gg.essential.elementa.dsl.minus
import gg.essential.elementa.dsl.percent
import gg.essential.elementa.dsl.pixels
import gg.essential.elementa.dsl.toConstraint
import cloud.glitchdev.rfu.config.categories.OtherSettings
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import java.awt.Color

object HudWindow : BaseWindow(false) {
    val backgroundColor = UIScheme.darkBackground.toConstraint()
    lateinit var background : UIBlock
    var isEditingOpen = false
    var isExportMode = false
    val hudElements : MutableList<AbstractHudElement> = mutableListOf()

    var currentContainerScreen: AbstractContainerScreen<*>? = null
        private set

    val isOnInventory: Boolean
        get() = currentContainerScreen != null

    var isInteractingWithHud: Boolean = false
        private set

    lateinit var vSnapLine: UIContainer
    lateinit var hSnapLine: UIContainer
    lateinit var infoText : UIWrappedText
    lateinit var exportBanner : UIWrappedText
    lateinit var resetButton : UIButton

    private var resetClickTimestamp = 0L
    private var isConfirmingReset = false

    enum class RenderPass {
        NONE,
        HUD,
        INVENTORY
    }

    var currentRenderPass: RenderPass = RenderPass.NONE
        private set

    init {
        create()

        registerHudRenderEvent { context, ticks ->
            if (!isEditingOpen) {
                currentRenderPass = RenderPass.HUD
                try {
                    extractRenderState(context, 0, 0, ticks)
                } finally {
                    currentRenderPass = RenderPass.NONE
                }
            }
        }

        registerShutdownEvent(0) {
            for(element in hudElements) {
                HudManager.updateElementConfig(element)
            }
        }

        registerTickEvent {
            if (isConfirmingReset && System.currentTimeMillis() - resetClickTimestamp > 10000L) {
                revertResetButton()
            }
        }

        registerJoinEvent(delayMillis = 500L) {
            resolvePositionsOnWorldJoin()
        }
    }

    fun resolvePositionsOnWorldJoin() {
        val screenWidth = window.getWidth()
        val screenHeight = window.getHeight()
        if (screenWidth <= 0f || screenHeight <= 0f) return

        if (!HudManager.hudData.hasInitializedDefaults) {
            if (HudManager.hudData.hudElements.isEmpty()) {
                HudManager.resetToDefaults(screenWidth, screenHeight, hudElements)
                return
            } else {
                HudManager.hudData.hasInitializedDefaults = true
                HudManager.hudFile.save()
            }
        }

        var anyResolved = false
        for (element in hudElements) {
            if (!HudManager.hasElementConfig(element.id)) {
                val calculated = DefaultHudManager.calculateDefaultPosition(element, screenWidth, screenHeight)
                element.currentX = calculated.x
                element.currentY = calculated.y
                element.scale = calculated.scale
                HudManager.hudData.update(element.id, calculated.x, calculated.y, calculated.scale)
                element.updateState()
                anyResolved = true
            }
        }
        if (anyResolved) {
            HudManager.hudFile.save()
        }
    }

    fun onInventoryOpened(screen: AbstractContainerScreen<*>) {
        currentContainerScreen = screen
        isInteractingWithHud = false
        for (element in hudElements) {
            element.updateState()
        }
    }

    fun onInventoryClosed() {
        currentContainerScreen = null
        isInteractingWithHud = false
        if (OtherSettings.patchElementaMemoryLeaks) {
            window.invalidateCachedConstraints()
        }
        for (element in hudElements) {
            element.updateState()
        }
    }

    fun renderOnInventory(context: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, deltaTicks: Float) {
        if (isEditingOpen) return
        currentRenderPass = RenderPass.INVENTORY
        try {
            extractRenderState(context, mouseX, mouseY, deltaTicks)
        } finally {
            currentRenderPass = RenderPass.NONE
        }
    }

    fun getHoveredElement(x: Double, y: Double): AbstractHudElement? {
        val fx = x.toFloat()
        val fy = y.toFloat()
        for (element in hudElements.asReversed()) {
            if (element.enabled && element.renderOnInventory && !element.isHidden() && element.isPointInside(fx, fy)) {
                return element
            }
        }
        return null
    }

    fun handleMouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (isEditingOpen) return false
        val hovered = getHoveredElement(mouseX, mouseY)
        if (hovered != null && hovered.isClickableOnInventory) {
            isInteractingWithHud = true
            hovered.mouseClick(mouseX, mouseY, button)
            return true
        }
        return false
    }

    fun handleMouseReleased(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (isEditingOpen) return false
        val wasInteracting = isInteractingWithHud
        window.mouseRelease()
        isInteractingWithHud = false
        return wasInteracting
    }

    fun handleMouseDragged(mouseX: Double, mouseY: Double, button: Int, dx: Double, dy: Double): Boolean {
        if (isEditingOpen) return false
        if (isInteractingWithHud) {
            return true
        }
        return false
    }

    fun handleMouseScrolled(mouseX: Double, mouseY: Double, scrollX: Double, scrollY: Double): Boolean {
        if (isEditingOpen) return false
        val hovered = getHoveredElement(mouseX, mouseY)
        if (hovered != null && hovered.isClickableOnInventory) {
            hovered.mouseScroll(scrollX, scrollY)
            return true
        }
        return false
    }

    fun openEditingGui() {
        resolvePositionsOnWorldJoin()
        isExportMode = false
        isEditingOpen = true
        updateState()
        for(element in hudElements) {
            element.openEdit(preview = false)
        }
        Gui.openGui(this)
    }

    fun openExportGui() {
        resolvePositionsOnWorldJoin()
        isExportMode = true
        isEditingOpen = true
        updateState()
        for(element in hudElements) {
            element.openEdit(preview = true)
        }
        Gui.openGui(this)
    }

    override fun onWindowClose() {
        isEditingOpen = false
        revertResetButton()
        updateState()

        if (isExportMode) {
            DefaultHudManager.exportLayoutToJson(window.getWidth(), window.getHeight(), hudElements)
            isExportMode = false
            for (element in hudElements) {
                element.closeEdit()
                HudManager.updateElementConfig(element)
            }
            HudManager.hudFile.save()
        } else {
            for (element in hudElements) {
                element.closeEdit()
                HudManager.updateElementConfig(element)
            }
        }
    }

    fun updateState() {
        background.setHidden(!isEditingOpen)
        if (::exportBanner.isInitialized) exportBanner.setHidden(!isExportMode)
        if (::infoText.isInitialized) infoText.setHidden(isExportMode)
        if (::resetButton.isInitialized) resetButton.setHidden(isExportMode)
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
        if (!isExportMode && ::infoText.isInitialized) {
            infoText.setHidden(!state)
        }
    }

    private fun handleResetButtonClick() {
        val now = System.currentTimeMillis()
        if (!isConfirmingReset) {
            isConfirmingReset = true
            resetClickTimestamp = now
            resetButton.updateText("Click again to confirm")
        } else {
            val elapsed = now - resetClickTimestamp
            if (elapsed < 250) {
                return
            }
            if (elapsed <= 10000L) {
                resetAllToDefaults()
                revertResetButton()
            } else {
                isConfirmingReset = true
                resetClickTimestamp = now
                resetButton.updateText("Click again to confirm")
            }
        }
    }

    fun revertResetButton() {
        isConfirmingReset = false
        resetClickTimestamp = 0L
        if (::resetButton.isInitialized) {
            resetButton.updateText("Reset HUD")
        }
    }

    fun resetAllToDefaults() {
        HudManager.resetToDefaults(window.getWidth(), window.getHeight(), hudElements)
        Chat.sendMessage(TextUtils.rfuLiteral("HUD has been reset to default positions.", TextStyle(TextColor.LIGHT_GREEN)))
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

        exportBanner = UIWrappedText(
            text = """§6HUD Layout Designer §7(All elements visible)§r
                     |§ePress §cESC§e to export to clipboard!§r""".trimMargin(),
            shadow = true,
            centered = true
        ).constrain {
            x = CenterConstraint()
            y = 20.pixels()
        } childOf background

        resetButton = UIButton("Reset HUD", radiusProps = 4f, onClick = {
            handleResetButtonClick()
        }).constrain {
            x = CenterConstraint()
            y = 100.percent() - 24.pixels()
            width = 130.pixels()
            height = 18.pixels()
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
        val existing = HudManager.getElementConfig(element)

        if (existing != null) {
            element.apply {
                currentX = existing.x
                currentY = existing.y
                scale = existing.scale
            }
        }
        element.updateState()
    }
}