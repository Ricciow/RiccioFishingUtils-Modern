package cloud.glitchdev.rfu.gui.hud

import cloud.glitchdev.rfu.events.managers.CloseConfigEvents.registerCloseConfigEvent
import cloud.glitchdev.rfu.events.managers.HypixelModApiEvents.registerLocationEvent
import cloud.glitchdev.rfu.gui.UIScheme
import cloud.glitchdev.rfu.gui.components.hud.UIFakeInventory
import cloud.glitchdev.rfu.gui.window.HudWindow
import cloud.glitchdev.rfu.utils.World
import cloud.glitchdev.rfu.utils.gui.setHidden
import gg.essential.elementa.components.UIBlock
import gg.essential.elementa.components.UIText
import gg.essential.elementa.components.Window
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.constraints.ChildBasedSizeConstraint
import gg.essential.elementa.constraints.ColorConstraint
import gg.essential.elementa.constraints.ScaledTextConstraint
import gg.essential.elementa.constraints.TextAspectConstraint
import gg.essential.elementa.dsl.childOf
import gg.essential.elementa.dsl.constrain
import gg.essential.elementa.dsl.minus
import gg.essential.elementa.dsl.percent
import gg.essential.elementa.dsl.pixels
import gg.essential.elementa.dsl.plus
import gg.essential.elementa.dsl.toConstraint
import gg.essential.universal.UKeyboard
import gg.essential.universal.UMatrixStack
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.round

abstract class AbstractHudElement(val id: String) : UIBlock() {
    private val selectionColor = UIScheme.secondaryColorOpaque.toConstraint()
    private val holdColor = UIScheme.secondaryColorDisabledOpaque.toConstraint()
    private val transparent = UIScheme.transparent.toConstraint()
    private val snapThreshold = 5f

    //Prevent overlapping on default positions
    open val defaultX = currentDefaultX
    open val defaultY = currentDefaultY.also {
        if (currentYCount < Y_LIMIT - 1) {
            currentDefaultY += Y_INCREMENT
            currentYCount += 1
        } else {
            currentDefaultX += X_INCREMENT
            currentDefaultY = Y_INITIAL_VALUE + Y_INCREMENT
            currentYCount = 0
        }
    }

    var currentX = defaultX
    var currentY = defaultY
    open val requirement: Boolean = true
    open val isElementActive: Boolean = true
    open val isOnInventory: Boolean
        get() = HudWindow.isOnInventory
    open val renderOnHud: Boolean = true
    open val renderOnInventory: Boolean = false
    open val isClickableOnInventory: Boolean = true
    var forcePreview: Boolean = false
    open val enabled: Boolean
        get() {
            if (forcePreview) return true
            if (HudWindow.isEditingOpen) return isEditing && requirement
            return requirement && (isEditing || isElementActive)
        }
    open var scale = 1f
    open val skyblockOnly = true

    protected var isEditing = false
    private var dragOffsetX = 0f
    private var dragOffsetY = 0f
    private var isDragging = false
    private val window : Window
        get() = Window.of(this)

    private var scaleTextEnabled = false
    private var scaleText = UIText("Scale: 1.00x").constrain {
        x = CenterConstraint()
        width = ScaledTextConstraint(1f)
        height = TextAspectConstraint()
    } childOf this

    init {
        this.constrain {
            x = defaultX.pixels()
            y = defaultY.pixels()
            width = ChildBasedSizeConstraint()
            height = ChildBasedSizeConstraint()
            isFloating = true
        }

        scaleText.hide()

        this.onMouseClick { event ->
            if (HudWindow.isEditingOpen && isEditing) {
                grabWindowFocus()
                isDragging = true
                dragOffsetX = event.absoluteX - this.getLeft()
                dragOffsetY = event.absoluteY - this.getTop()
                scaleTextEnabled = true
                updateState()
            }
        }

        this.onMouseDrag { mouseX, mouseY, _ ->
            if (HudWindow.isEditingOpen && isEditing && isDragging) {
                grabWindowFocus()
                updatePosition(mouseX, mouseY)
                scaleTextEnabled = true
            }
        }

        this.onMouseScroll { event ->
            if (HudWindow.isEditingOpen && isEditing) {
                grabWindowFocus()
                val shiftDown = UKeyboard.isShiftKeyDown()
                val ctrlDown = UKeyboard.isCtrlKeyDown()

                var supression = when {
                    shiftDown && ctrlDown -> 1000
                    shiftDown || ctrlDown -> 100
                    else -> 10
                }

                val effect = event.delta.toFloat() / supression

                scale = round(max(0.3f, scale + effect) * 1000) / 1000

                scaleTextEnabled = true

                updateState()
            }
        }

        this.onMouseRelease {
            if (isDragging) {
                isDragging = false
                HudWindow.showSnapLines(null, null)
            }

            if (scaleTextEnabled) {
                scaleTextEnabled = false
                updateState()
            }
        }

        this.onFocus {
            if (HudWindow.isEditingOpen && isEditing) {
                scaleTextEnabled = true
                HudWindow.setInfotextState(false)
                updateState()
            }
        }

        this.onFocusLost {
            scaleTextEnabled = false
            if (HudWindow.isEditingOpen && isEditing) {
                HudWindow.setInfotextState(true)
            }
            updateState()
        }

        this.onKeyType { _, id ->
            if (HudWindow.isEditingOpen && isEditing && id == UKeyboard.KEY_ESCAPE) {
                HudWindow.closeScreen()
            }
        }
    }

    private fun updatePosition(mouseX: Float, mouseY: Float) {
        val absoluteMouseX = this.getLeft() + mouseX
        val absoluteMouseY = this.getTop() + mouseY
        currentX = absoluteMouseX - dragOffsetX
        currentY = absoluteMouseY - dragOffsetY

        var snapLineX: Float? = null
        var snapLineY: Float? = null

        val isSnappingSuppressed = UKeyboard.isShiftKeyDown() || UKeyboard.isCtrlKeyDown()

        if (!isSnappingSuppressed) {
            val (newX, lineX) = resolveSnap(currentX, this.getWidth(), window.getWidth()) { other ->
                other.getLeft() to other.getRight()
            }

            val (newY, lineY) = resolveSnap(currentY, this.getHeight(), window.getHeight()) { other ->
                other.getTop() to other.getBottom()
            }

            currentX = newX
            currentY = newY
            snapLineX = lineX
            snapLineY = lineY
        }

        HudWindow.showSnapLines(snapLineX, snapLineY)

        currentX = currentX.coerceIn(0f, window.getWidth() - this.getWidth())
        currentY = currentY.coerceIn(0f, window.getHeight() - this.getHeight())

        updateState()
    }

    /**
     * Calculates snapping for a single axis.
     * Checks:
     * 1. Window Center
     * 2. Edges of all other elements
     * Returns the position that requires the least movement.
     */
    private fun resolveSnap(
        currentPos: Float,
        size: Float,
        windowDimension: Float,
        getBounds: (AbstractHudElement) -> Pair<Float, Float>
    ): Pair<Float, Float?> {
        var bestPos = currentPos
        var bestSnapLine: Float? = null
        var minDistance = snapThreshold

        // Verify Window Snapping
        val windowCenter = windowDimension / 2f
        val centerTarget = windowCenter - (size / 2f)
        val distToCenter = abs(currentPos - centerTarget)

        if (distToCenter < minDistance) {
            minDistance = distToCenter
            bestPos = centerTarget
            bestSnapLine = windowCenter
        }

        if (HudWindow.isEditingOpen && HudWindow.currentEditTarget == HudWindow.EditTarget.INVENTORY) {
            val invDimension = if (windowDimension == window.getWidth()) UIFakeInventory.INVENTORY_WIDTH else UIFakeInventory.INVENTORY_HEIGHT
            val invStart = (windowDimension - invDimension) / 2f
            val invEnd = invStart + invDimension

            val invCandidates = listOf(
                invStart to invStart,
                invEnd to invEnd,
                (invStart - size) to invStart,
                (invEnd - size) to invEnd
            )

            for ((pos, line) in invCandidates) {
                val distance = abs(currentPos - pos)

                if (distance < minDistance) {
                    minDistance = distance
                    bestPos = pos
                    bestSnapLine = line
                }
            }
        }

        for (other in HudWindow.hudElements) {
            if (other === this || !other.enabled) continue

            val (start, end) = getBounds(other)

            val candidates = listOf(
                start to start,                  // Align Top/Left to Top/Left
                end to end,                      // Align Top/Left to Bottom/Right
                (start - size) to start,         // Align Bottom/Right to Top/Left
                (end - size) to end              // Align Bottom/Right to Bottom/Right
            )

            for ((pos, line) in candidates) {
                val distance = abs(currentPos - pos)

                if (distance < minDistance) {
                    minDistance = distance
                    bestPos = pos
                    bestSnapLine = line
                }
            }
        }

        return Pair(bestPos, bestSnapLine)
    }

    fun initialize() {
        HudWindow.registerHudElement(this)

        registerCloseConfigEvent {
            updateState()
        }

        registerLocationEvent {
            updateState()
        }

        onInitialize()
    }

    private fun currentColor() : ColorConstraint = if (isEditing) if(isDragging) holdColor else selectionColor else transparent

    fun updateState() {
        if(hasParent) {
            scaleText.setHidden(!scaleTextEnabled)

            val gap = 5f
            scaleText.constrain {
                y = when {
                    getBottom() + gap + 11 < window.getBottom() -> 100.percent() + gap.pixels()
                    else -> (-gap - 9).pixels()
                }
            }

            scaleText.setText("Scale: %.3fx".format(scale))

            this.constrain {
                color = currentColor()
                x = currentX.pixels()
                y = currentY.pixels()
                width = if (scaleTextEnabled) ChildBasedSizeConstraint() - scaleText.getWidth().pixels() else ChildBasedSizeConstraint()
                height = if (scaleTextEnabled) ChildBasedSizeConstraint() - scaleText.getHeight().pixels() else ChildBasedSizeConstraint()
            }

            val isHidden = if (HudWindow.isEditingOpen) {
                !forcePreview && (!isEditing || !requirement)
            } else {
                (!enabled || skyblockOnly && !World.isInSkyblock) && !forcePreview
            }

            this.setHidden(isHidden)

            onUpdateState()
        }
    }

    fun openEdit(preview: Boolean = false) {
        forcePreview = preview
        isEditing = true
        updateState()
        onOpenEdit()
    }

    fun closeEdit() {
        forcePreview = false
        isEditing = false
        scaleTextEnabled = false
        updateState()
        onCloseEdit()
    }

    //Methods that can be used to hook on children classes
    open fun onInitialize() {}
    open fun onUpdateState() {}
    open fun onOpenEdit() {}
    open fun onCloseEdit() {}

    protected open fun shouldDrawInCurrentPass(): Boolean {
        if (HudWindow.isEditingOpen) {
            return isEditing && when (HudWindow.currentEditTarget) {
                HudWindow.EditTarget.HUD -> renderOnHud
                HudWindow.EditTarget.INVENTORY -> renderOnInventory
            }
        }
        if (forcePreview) return true

        return when (HudWindow.currentRenderPass) {
            HudWindow.RenderPass.HUD -> renderOnHud
            HudWindow.RenderPass.INVENTORY -> isOnInventory && renderOnInventory
            HudWindow.RenderPass.NONE -> !isOnInventory && renderOnHud
        }
    }

    override fun draw(matrixStack: UMatrixStack) {
        if (!shouldDrawInCurrentPass()) return
        super.draw(matrixStack)
    }

    companion object {
        private const val Y_LIMIT = 8
        private const val Y_INCREMENT = 35f
        private const val X_INCREMENT = 150f
        private const val X_INITIAL_VALUE = 10f
        private const val Y_INITIAL_VALUE = 10f

        private var currentYCount = 0
        private var currentDefaultX = X_INITIAL_VALUE
        private var currentDefaultY = Y_INITIAL_VALUE
    }
}