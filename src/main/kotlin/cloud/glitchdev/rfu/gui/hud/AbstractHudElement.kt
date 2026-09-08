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
    private var lastSnapLineX: Float? = null
    private var lastSnapLineY: Float? = null
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
                lastSnapLineX = null
                lastSnapLineY = null
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
            val (newX, lineX) = resolveSnap(
                currentPos = currentX,
                size = this.getWidth(),
                windowDimension = window.getWidth(),
                currentOrthogonalPos = currentY,
                orthogonalSize = this.getHeight(),
                lastSnapLine = lastSnapLineX,
                getBounds = { it.getLeft() to it.getRight() },
                getOrthogonalBounds = { it.getTop() to it.getBottom() }
            )

            val (newY, lineY) = resolveSnap(
                currentPos = currentY,
                size = this.getHeight(),
                windowDimension = window.getHeight(),
                currentOrthogonalPos = currentX,
                orthogonalSize = this.getWidth(),
                lastSnapLine = lastSnapLineY,
                getBounds = { it.getTop() to it.getBottom() },
                getOrthogonalBounds = { it.getLeft() to it.getRight() }
            )

            currentX = newX
            currentY = newY
            snapLineX = lineX
            snapLineY = lineY
            lastSnapLineX = lineX
            lastSnapLineY = lineY
        } else {
            lastSnapLineX = null
            lastSnapLineY = null
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
     * 2. Inventory boundary (if in INVENTORY edit mode)
     * 3. Edges of other enabled HUD elements, applying orthogonal proximity penalty/gating
     * Returns the position that best satisfies snapping criteria.
     */
    private fun resolveSnap(
        currentPos: Float,
        size: Float,
        windowDimension: Float,
        currentOrthogonalPos: Float,
        orthogonalSize: Float,
        lastSnapLine: Float?,
        getBounds: (AbstractHudElement) -> Pair<Float, Float>,
        getOrthogonalBounds: (AbstractHudElement) -> Pair<Float, Float>
    ): Pair<Float, Float?> {
        var bestPos = currentPos
        var bestSnapLine: Float? = null
        var bestScore = snapThreshold

        fun checkCandidate(pos: Float, line: Float, orthPenalty: Float) {
            val dist = abs(currentPos - pos)
            var score = dist + orthPenalty
            if (lastSnapLine != null && abs(line - lastSnapLine) < 0.5f) {
                score -= 1.5f
            }
            if (score < bestScore) {
                bestScore = score
                bestPos = pos
                bestSnapLine = line
            }
        }

        val isStartSide = (currentPos + size / 2f) < (windowDimension / 2f)
        val startSideBonus = if (isStartSide) 1.0f else 0f
        val endSideBonus = if (!isStartSide) 1.0f else 0f

        fun evaluateBoundingBox(
            start: Float,
            end: Float,
            orthStart: Float,
            orthEnd: Float,
            isGlobal: Boolean = false
        ) {
            val orthPenalty = if (isGlobal) {
                0f
            } else {
                val thisOrthCenter = currentOrthogonalPos + (orthogonalSize / 2f)
                val otherOrthCenter = (orthStart + orthEnd) / 2f
                val orthDist = abs(thisOrthCenter - otherOrthCenter)
                val isOverlapping = (currentOrthogonalPos <= orthEnd) && (currentOrthogonalPos + orthogonalSize >= orthStart)
                if (!isOverlapping && orthDist > MAX_SNAP_ORTHOGONAL_DISTANCE) return
                orthDist * 0.02f
            }

            checkCandidate(start, start, orthPenalty - startSideBonus)          // Start to Start (Left-to-Left / Top-to-Top)
            checkCandidate(end, end, orthPenalty)                               // Start to End (Adjacent)
            checkCandidate(start - size, start, orthPenalty)                    // End to Start (Adjacent)
            checkCandidate(end - size, end, orthPenalty - endSideBonus)         // End to End (Right-to-Right / Bottom-to-Bottom)
        }

        val windowCenter = windowDimension / 2f
        val centerTarget = windowCenter - (size / 2f)
        checkCandidate(centerTarget, windowCenter, 0f)

        if (HudWindow.isEditingOpen && HudWindow.currentEditTarget == HudWindow.EditTarget.INVENTORY) {
            val isHorizontal = windowDimension == window.getWidth()
            val invStart = if (isHorizontal) {
                UIFakeInventory.getInventoryLeft(windowDimension)
            } else {
                UIFakeInventory.getInventoryTop(windowDimension)
            }
            val invDimension = if (isHorizontal) UIFakeInventory.INVENTORY_WIDTH else UIFakeInventory.INVENTORY_HEIGHT
            val invEnd = invStart + invDimension

            val windowOrthDimension = if (isHorizontal) window.getHeight() else window.getWidth()
            val invOrthStart = if (isHorizontal) {
                UIFakeInventory.getInventoryTop(windowOrthDimension)
            } else {
                UIFakeInventory.getInventoryLeft(windowOrthDimension)
            }
            val invOrthDimension = if (isHorizontal) UIFakeInventory.INVENTORY_HEIGHT else UIFakeInventory.INVENTORY_WIDTH
            val invOrthEnd = invOrthStart + invOrthDimension

            evaluateBoundingBox(invStart, invEnd, invOrthStart, invOrthEnd, isGlobal = true)
        }

        for (other in HudWindow.hudElements) {
            if (other === this || !other.enabled) continue

            val (start, end) = getBounds(other)
            val (orthStart, orthEnd) = getOrthogonalBounds(other)

            evaluateBoundingBox(start, end, orthStart, orthEnd, isGlobal = false)
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
        isDragging = false
        lastSnapLineX = null
        lastSnapLineY = null
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
            HudWindow.RenderPass.HUD -> !isOnInventory && renderOnHud
            HudWindow.RenderPass.INVENTORY -> isOnInventory && renderOnInventory
            HudWindow.RenderPass.NONE -> !isOnInventory && renderOnHud
        }
    }

    override fun draw(matrixStack: UMatrixStack) {
        if (!shouldDrawInCurrentPass()) return
        super.draw(matrixStack)
    }

    companion object {
        private const val MAX_SNAP_ORTHOGONAL_DISTANCE = 300f
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