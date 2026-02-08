package cloud.glitchdev.rfu.gui.hud

import cloud.glitchdev.rfu.events.managers.CloseConfigEvents.registerCloseConfigEvent
import cloud.glitchdev.rfu.events.managers.HypixelModApiEvents.registerLocationEvent
import cloud.glitchdev.rfu.gui.UIScheme
import cloud.glitchdev.rfu.gui.window.HudWindow
import cloud.glitchdev.rfu.utils.World
import cloud.glitchdev.rfu.utils.dsl.roundToDecimal
import cloud.glitchdev.rfu.utils.gui.setHidden
import gg.essential.elementa.components.UIBlock
import gg.essential.elementa.components.Window
import gg.essential.elementa.constraints.ChildBasedSizeConstraint
import gg.essential.elementa.constraints.ColorConstraint
import gg.essential.elementa.dsl.constrain
import gg.essential.elementa.dsl.pixels
import gg.essential.elementa.dsl.toConstraint
import gg.essential.universal.UKeyboard
import kotlin.math.abs
import kotlin.math.max

abstract class AbstractHudElement(val id: String) : UIBlock() {
    private val selectionColor = UIScheme.secondaryColorOpaque.toConstraint()
    private val holdColor = UIScheme.secondaryColorDisabledOpaque.toConstraint()
    private val transparent = UIScheme.transparent.toConstraint()
    private val snapThreshold = 5f

    open val defaultX = 10f
    open val defaultY = 10f
    var currentX = defaultX
    var currentY = defaultY
    open val enabled
        get() = isEditing
    open var scale = 1f
    open val skyblockOnly = true

    protected var isEditing = false
    private var dragOffsetX = 0f
    private var dragOffsetY = 0f
    private var isDragging = false
    private val window : Window
        get() = Window.of(this)

    init {
        this.constrain {
            x = defaultX.pixels()
            y = defaultY.pixels()
            width = ChildBasedSizeConstraint()
            height = ChildBasedSizeConstraint()
            isFloating = true
        }

        this.onMouseClick { event ->
            if (HudWindow.isEditingOpen && isEditing) {
                isDragging = true
                dragOffsetX = event.absoluteX - this.getLeft()
                dragOffsetY = event.absoluteY - this.getTop()
                updateState()
            }
        }

        this.onMouseDrag { mouseX, mouseY, _ ->
            if (isDragging) {
                updatePosition(mouseX, mouseY)
            }
        }

        this.onMouseScroll { event ->
            val effect = event.delta.toFloat()/10

            if(scale >= 1) {
                scale *= 1f + effect
                scale = scale.roundToDecimal()
            } else if (scale >= 0.3f) {
                scale = max(0.3f, scale + effect)
            } else {
                scale = 0.3f
            }

            updateState()
        }

        this.onMouseRelease {
            if (isDragging) {
                isDragging = false
                HudWindow.showSnapLines(null, null)
            }
            updateState()
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
            val (newX, lineX) = resolveSnap(currentX, this.getWidth()) { other ->
                other.getLeft() to other.getRight()
            }

            val (newY, lineY) = resolveSnap(currentY, this.getHeight()) { other ->
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
     * Generic helper to calculate snapping for a single axis.
     * Returns a Pair(NewPosition, SnapLineCoordinate?)
     */
    private fun resolveSnap(
        currentPos: Float,
        size: Float,
        getBounds: (AbstractHudElement) -> Pair<Float, Float>
    ): Pair<Float, Float?> {
        for (other in HudWindow.hudElements) {
            if (other === this || !other.enabled) continue

            val (otherStart, otherEnd) = getBounds(other)

            if (abs(currentPos - otherStart) < snapThreshold) return Pair(otherStart, otherStart)
            if (abs(currentPos - otherEnd) < snapThreshold) return Pair(otherEnd, otherEnd)
            if (abs((currentPos + size) - otherStart) < snapThreshold) return Pair(otherStart - size, otherStart)
            if (abs((currentPos + size) - otherEnd) < snapThreshold) return Pair(otherEnd - size, otherEnd)
        }

        return Pair(currentPos, null)
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
        this.constrain {
            color = currentColor()
            x = currentX.pixels()
            y = currentY.pixels()
        }

        this.setHidden(!enabled || skyblockOnly && !World.isInSkyblock)

        onUpdateState()
    }

    fun openEdit() {
        isEditing = true
        updateState()
        onOpenEdit()
    }

    fun closeEdit() {
        isEditing = false
        updateState()
        onCloseEdit()
    }

    //Methods that can be used to hook on children classes
    open fun onInitialize() {}
    open fun onUpdateState() {}
    open fun onOpenEdit() {}
    open fun onCloseEdit() {}
}