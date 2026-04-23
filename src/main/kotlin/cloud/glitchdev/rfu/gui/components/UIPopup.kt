package cloud.glitchdev.rfu.gui.components

import cloud.glitchdev.rfu.gui.UIScheme
import cloud.glitchdev.rfu.gui.UIScheme.increaseOpacity
import gg.essential.elementa.components.UIBlock
import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.components.UIRoundedRectangle
import gg.essential.elementa.components.UIWrappedText
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.constraints.FillConstraint
import gg.essential.elementa.constraints.RelativeWindowConstraint
import gg.essential.elementa.dsl.childOf
import gg.essential.elementa.dsl.constrain
import gg.essential.elementa.dsl.minus
import gg.essential.elementa.dsl.percent
import gg.essential.elementa.dsl.pixels
import gg.essential.elementa.dsl.toConstraint
import java.awt.Color

class UIPopup(
    val radiusPopup: Float,
    var text: String,
    val isBordered: Boolean = false,
    var onConfirm: (() -> Unit)? = null
) : UIBlock(), Colorable {
    var backgroundColor = Color.BLACK.increaseOpacity(127).toConstraint()
    var textColor = UIScheme.primaryTextColor.toConstraint()
    var postConfirmationTextColor = UIScheme.postConfirmationColor.toConstraint()
    var primaryColor = UIScheme.secondaryColorOpaque.toConstraint()
    var innerColor = Color.BLACK.toConstraint()
    var borderWidth = 1f

    var buttonPrimaryColor = UIScheme.secondaryColorOpaque.toConstraint()
    var buttonHoverColor = UIScheme.secondaryColor.toConstraint()
    var buttonTextColor = UIScheme.primaryTextColor.toConstraint()
    var buttonHoverTextColor = UIScheme.primaryTextColor.toConstraint()

    lateinit var uiText : UIWrappedText
    lateinit var popupContainer : UIRoundedRectangle
    lateinit var innerBg : UIRoundedRectangle
    private val buttons = mutableListOf<UIButton>()
    private lateinit var okButton: UIButton
    private lateinit var confirmCancelContainer: UIContainer
    var postConfirmationText: String? = null

    init {
        this.hide()
        create()
    }

    fun show(text: String, postConfirmationText: String? = null, onConfirm: (() -> Unit)? = null) {
        this.text = text
        this.postConfirmationText = postConfirmationText
        if (::uiText.isInitialized) {
            uiText.setText(text)
            uiText.constrain { color = textColor }
        }

        this.onConfirm = onConfirm

        if (onConfirm == null) {
            okButton.unhide()
            confirmCancelContainer.hide()
        } else {
            okButton.hide()
            confirmCancelContainer.unhide()
        }

        unhide()
    }

    fun create() {
        this.constrain {
            x = RelativeWindowConstraint(0f)
            y = RelativeWindowConstraint(0f)
            width = RelativeWindowConstraint(1f)
            height = RelativeWindowConstraint(1f)
            color = backgroundColor
            isFloating = true
        }

        this.onMouseClick {
            it.stopPropagation()
        }

        popupContainer = UIRoundedRectangle(radiusPopup).constrain {
            x = CenterConstraint()
            y = CenterConstraint()
            width = 33.percent()
            height = 33.percent()
            color = primaryColor
        } childOf this

        val contentParent = if (isBordered) {
            innerBg = UIRoundedRectangle(radiusPopup).constrain {
                x = CenterConstraint()
                y = CenterConstraint()
                width = 100.percent - (borderWidth * 2).pixels
                height = 100.percent - (borderWidth * 2).pixels
                color = innerColor
            } childOf popupContainer
            innerBg
        } else {
            popupContainer
        }

        val container = UIContainer().constrain {
            x = CenterConstraint()
            y = CenterConstraint()
            width = 90.percent()
            height = 90.percent()
        } childOf contentParent

        okButton = UIButton("Ok", 5f, isBordered = isBordered){
            this@UIPopup.hide()
        }.constrain {
            x = CenterConstraint()
            y = 100.percent() - 20.pixels()
            width = 100.percent()
            height = 20.pixels()
        } childOf container
        buttons.add(okButton)

        confirmCancelContainer = UIContainer().constrain {
            x = CenterConstraint()
            y = 100.percent() - 20.pixels()
            width = 100.percent()
            height = 20.pixels()
        } childOf container

        val confirmButton = UIButton("Confirm", 5f, isBordered = isBordered) {
            this@UIPopup.onConfirm?.invoke()
            if (this@UIPopup.postConfirmationText != null) {
                this@UIPopup.uiText.setText(this@UIPopup.postConfirmationText!!)
                this@UIPopup.uiText.constrain { color = postConfirmationTextColor }
                this@UIPopup.confirmCancelContainer.hide()
                this@UIPopup.okButton.unhide()
            } else {
                this@UIPopup.hide()
            }
        }.constrain {
            x = 0.pixels()
            y = CenterConstraint()
            width = 45.percent()
            height = 100.percent()
        } childOf confirmCancelContainer
        buttons.add(confirmButton)

        val cancelButton = UIButton("Cancel", 5f, isBordered = isBordered) {
            this@UIPopup.hide()
        }.constrain {
            x = 0.pixels(true)
            y = CenterConstraint()
            width = 45.percent()
            height = 100.percent()
        } childOf confirmCancelContainer
        buttons.add(cancelButton)

        uiText = UIWrappedText(text).constrain {
            x = CenterConstraint()
            y = 0.pixels()
            width = 100.percent()
            height = FillConstraint() - 25.pixels()
            color = textColor
        } childOf container

        refreshButtonColors()
    }

    private fun refreshButtonColors() {
        buttons.forEach { button ->
            button.colors {
                primaryColor = this@UIPopup.buttonPrimaryColor
                hoverColor = this@UIPopup.buttonHoverColor
                textColor = this@UIPopup.buttonTextColor
                hoverTextColor = this@UIPopup.buttonHoverTextColor
            }
        }
    }

    override fun refreshColors() {
        this.constrain { color = backgroundColor }
        if (::popupContainer.isInitialized) popupContainer.constrain { color = primaryColor }
        if (isBordered && ::innerBg.isInitialized) innerBg.constrain { color = innerColor }
        if (::uiText.isInitialized) {
            if (postConfirmationText != null && uiText.getText() == postConfirmationText) {
                uiText.constrain { color = postConfirmationTextColor }
            } else {
                uiText.constrain { color = textColor }
            }
        }
        refreshButtonColors()
    }
}