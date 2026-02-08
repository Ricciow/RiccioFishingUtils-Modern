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
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

abstract class AbstractHudElement(val id: String) : UIBlock() {
    private val selectionColor = UIScheme.secondaryColorOpaque.toConstraint()
    private val holdColor = UIScheme.secondaryColorDisabledOpaque.toConstraint()
    private val transparent = UIScheme.transparent.toConstraint()

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
                val absoluteMouseX = this.getLeft() + mouseX
                val absoluteMouseY = this.getTop() + mouseY

                currentX = absoluteMouseX - dragOffsetX
                currentY = absoluteMouseY - dragOffsetY

                // Snapping
                val snapThreshold = 5f
                val thisWidth = this.getWidth()
                val thisHeight = this.getHeight()
                var snappedX = false
                var snappedY = false

                for (other in HudWindow.hudElements) {
                    if (other === this || !other.enabled) continue

                    val otherLeft = other.getLeft()
                    val otherRight = other.getRight()
                    val otherTop = other.getTop()
                    val otherBottom = other.getBottom()

                    // X-axis snapping
                    if (!snappedX) {
                        if (abs(currentX - otherLeft) < snapThreshold) {
                            currentX = otherLeft
                            snappedX = true
                        } else if (abs(currentX - otherRight) < snapThreshold) {
                            currentX = otherRight
                            snappedX = true
                        } else if (abs(currentX + thisWidth - otherLeft) < snapThreshold) {
                            currentX = otherLeft - thisWidth
                            snappedX = true
                        } else if (abs(currentX + thisWidth - otherRight) < snapThreshold) {
                            currentX = otherRight - thisWidth
                            snappedX = true
                        }
                    }

                    // Y-axis snapping
                    if (!snappedY) {
                        if (abs(currentY - otherTop) < snapThreshold) {
                            currentY = otherTop
                            snappedY = true
                        } else if (abs(currentY - otherBottom) < snapThreshold) {
                            currentY = otherBottom
                            snappedY = true
                        } else if (abs(currentY + thisHeight - otherTop) < snapThreshold) {
                            currentY = otherTop - thisHeight
                            snappedY = true
                        } else if (abs(currentY + thisHeight - otherBottom) < snapThreshold) {
                            currentY = otherBottom - thisHeight
                            snappedY = true
                        }
                    }

                    if (snappedX && snappedY) break
                }

                //Limit within screen
                currentX = min(max(currentX, 0f), window.getWidth()-this.getWidth())
                currentY = min(max(currentY, 0f), window.getHeight()-this.getHeight())

                updateState()
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
            }
            updateState()
        }
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