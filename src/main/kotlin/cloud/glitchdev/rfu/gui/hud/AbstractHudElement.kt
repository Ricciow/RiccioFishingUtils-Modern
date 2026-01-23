package cloud.glitchdev.rfu.gui.hud

import cloud.glitchdev.rfu.events.managers.CloseConfigEvents.registerCloseConfigEvent
import cloud.glitchdev.rfu.gui.UIScheme
import cloud.glitchdev.rfu.gui.window.HudWindow
import cloud.glitchdev.rfu.utils.dsl.roundToDecimal
import cloud.glitchdev.rfu.utils.gui.setHidden
import gg.essential.elementa.components.UIBlock
import gg.essential.elementa.constraints.ChildBasedSizeConstraint
import gg.essential.elementa.dsl.constrain
import gg.essential.elementa.dsl.pixels
import gg.essential.elementa.dsl.toConstraint
import kotlin.math.max

abstract class AbstractHudElement(val id: String) : UIBlock() {
    private val selectionColor = UIScheme.secondaryColorOpaque.toConstraint()
    private val transparent = UIScheme.transparent.toConstraint()

    open val defaultX = 10f
    open val defaultY = 10f
    var currentX = defaultX
    var currentY = defaultY
    open val enabled = false
    open var scale = 1f

    protected var isEditing = false
    private var dragOffsetX = 0f
    private var dragOffsetY = 0f
    private var isDragging = false

    init {
        this.constrain {
            x = defaultX.pixels()
            y = defaultY.pixels()
            width = ChildBasedSizeConstraint()
            height = ChildBasedSizeConstraint()
        }

        this.onMouseClick { event ->
            if (HudWindow.isEditingOpen && isEditing) {
                isDragging = true
                dragOffsetX = event.absoluteX - this.getLeft()
                dragOffsetY = event.absoluteY - this.getTop()
            }
        }

        this.onMouseDrag { mouseX, mouseY, _ ->
            if (isDragging) {
                val absoluteMouseX = this.getLeft() + mouseX
                val absoluteMouseY = this.getTop() + mouseY

                currentX = absoluteMouseX - dragOffsetX
                currentY = absoluteMouseY - dragOffsetY

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
        }
    }

    fun initialize() {
        HudWindow.registerHudElement(this)

        registerCloseConfigEvent {
            updateState()
        }
    }

    fun updateState() {
        this.constrain {
            color = if (isEditing) selectionColor else transparent
            x = currentX.pixels()
            y = currentY.pixels()
        }

        this.setHidden(!enabled)

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

    open fun onUpdateState() {}
    open fun onOpenEdit() {}
    open fun onCloseEdit() {}
}