package cloud.glitchdev.rfu.gui.components

import cloud.glitchdev.rfu.gui.UIScheme
import cloud.glitchdev.rfu.utils.gui.setHidden
import cloud.glitchdev.rfu.utils.gui.height as textHeight
import cloud.glitchdev.rfu.utils.gui.width as textWidth
import gg.essential.elementa.components.UIImage
import gg.essential.elementa.components.UIRoundedRectangle
import gg.essential.elementa.components.UIText
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.constraints.TextAspectConstraint
import gg.essential.elementa.constraints.animation.Animations
import gg.essential.elementa.dsl.animate
import gg.essential.elementa.dsl.childOf
import gg.essential.elementa.dsl.constrain
import gg.essential.elementa.dsl.minus
import gg.essential.elementa.dsl.percent
import gg.essential.elementa.dsl.pixels
import gg.essential.elementa.dsl.times
import gg.essential.elementa.dsl.toConstraint
import gg.essential.universal.UMatrixStack
import java.awt.Color

/**
 * Simple Button Component
 */
class UIButton(
    var text: String,
    val radiusProps: Float = 0f,
    val image: UIImage? = null,
    val baseTextScale: Float = 1.0f,
    val isBordered: Boolean = false,
    var onClick: () -> Unit = {}
) : UIRoundedRectangle(radiusProps), Colorable {
    var primaryColor = UIScheme.secondaryColorOpaque.toConstraint()
    var hoverColor = UIScheme.secondaryColor.toConstraint()
    var textColor = UIScheme.primaryTextColor.toConstraint()
    var hoverTextColor = UIScheme.primaryTextColor.toConstraint()
    var secondaryTextColor = UIScheme.secondaryTextColor.toConstraint()
    var disabledColor = UIScheme.secondaryColorDisabledOpaque.toConstraint()
    var innerColor = UIScheme.pfCardBg.toConstraint()
    var borderWidth = UIScheme.pfCardBorderWidth
    val hoverDuration = UIScheme.HOVER_EFFECT_DURATION
    val clickDuration = 0.1f

    var disabled = false
        set(value) {
            field = value
            refreshColors()
        }

    lateinit var textArea : UIText
    lateinit var innerBg : UIRoundedRectangle
    private var isButtonHovered = false
    private var isTextHovered = false
    private var isImageHovered = false
    private var lastWidth = -1f
    private var lastHeight = -1f

    init {
        create()
    }

    fun updateText(text : String) {
        this.text = text
        if (::textArea.isInitialized) {
            textArea.setText(text)
            updateFontSize()
        }
    }

    fun create() {
        this.constrain {
            color = primaryColor
        }

        val contentParent = if (isBordered) {
            innerBg = UIRoundedRectangle(radiusProps).constrain {
                x = CenterConstraint()
                y = CenterConstraint()
                width = 100.percent - (borderWidth * 2).pixels
                height = 100.percent - (borderWidth * 2).pixels
                color = innerColor
            } childOf this
            innerBg
        } else {
            this
        }

        val initialHeight = 9f * baseTextScale
        textArea = UIText(text).constrain {
            x = CenterConstraint()
            y = CenterConstraint()
            // Will be updated by updateFontSize
            height = initialHeight.pixels()
            width = TextAspectConstraint()
            color = textColor
        } childOf contentParent

        textArea.onMouseEnter {
            if (!disabled) {
                isTextHovered = true
                textArea.animate {
                    setColorAnimation(Animations.IN_EXP, hoverDuration, hoverTextColor)
                }
            }
        }.onMouseLeave {
            if (!disabled) {
                isTextHovered = false
                textArea.animate {
                    setColorAnimation(Animations.IN_EXP, hoverDuration, textColor)
                }
            }
        }

        if(image != null) {
            textArea.setHidden(true)
            image.constrain {
                x = CenterConstraint()
                y = CenterConstraint()
                width = 70.percent()
                height = 70.percent()
                color = textColor
            } childOf contentParent

            image.onMouseEnter {
                if (!disabled) {
                    isImageHovered = true
                    image.animate {
                        setColorAnimation(Animations.IN_EXP, hoverDuration, hoverTextColor)
                    }
                }
            }.onMouseLeave {
                if (!disabled) {
                    isImageHovered = false
                    image.animate {
                        setColorAnimation(Animations.IN_EXP, hoverDuration, textColor)
                    }
                }
            }
        }

        this.onMouseClick { event ->
            event.stopPropagation()
            if(!disabled) {
                onClick()
                textArea.animate {
                    setColorAnimation(Animations.IN_EXP, clickDuration, secondaryTextColor)
                }
                image?.animate {
                    setColorAnimation(Animations.IN_EXP, clickDuration, secondaryTextColor)
                }
            }
        }
        .onMouseRelease {
            if(!disabled) {
                val targetTextColor = if (isTextHovered) hoverTextColor else textColor
                textArea.animate {
                    setColorAnimation(Animations.IN_EXP, clickDuration, targetTextColor)
                }
                image?.let {
                    val targetImageColor = if (isImageHovered) hoverTextColor else textColor
                    it.animate {
                        setColorAnimation(Animations.IN_EXP, clickDuration, targetImageColor)
                    }
                }
            }
        }
        .onMouseEnter {
            isButtonHovered = true
            if(!disabled) {
                this.animate {
                    setColorAnimation(Animations.IN_EXP, hoverDuration, hoverColor)
                }
            }
        }
        .onMouseLeave {
            isButtonHovered = false
            if(!disabled) {
                this.animate {
                    setColorAnimation(Animations.IN_EXP, hoverDuration, primaryColor)
                }
            }
        }

    }

    override fun draw(matrixStack: UMatrixStack) {
        val currentWidth = this.getWidth()
        val currentHeight = this.getHeight()
        if (currentWidth != lastWidth || currentHeight != lastHeight) {
            lastWidth = currentWidth
            lastHeight = currentHeight
            updateFontSize()
        }
        super.draw(matrixStack)
    }

    private fun updateFontSize() {
        if (!::textArea.isInitialized) return
        var scale = baseTextScale
        val txt = textArea.getText()

        while (scale > 0.1f && (
            txt.textWidth(scale) > this.getWidth() * 0.9f || 
            txt.textHeight(scale) > this.getHeight() * 0.9f
        )) {
            scale -= 0.1f
        }
        
        val newHeight = 9f * scale
        textArea.constrain {
            height = newHeight.pixels()
        }
    }

    override fun refreshColors() {
        this.constrain { color = if (disabled) disabledColor else if (isButtonHovered) hoverColor else primaryColor }
        if (::textArea.isInitialized) {
            textArea.constrain { color = if (disabled) secondaryTextColor else if (isTextHovered) hoverTextColor else textColor }
        }
        if (image != null) {
            image.constrain { color = if (disabled) secondaryTextColor else if (isImageHovered) hoverTextColor else textColor }
        }
        if (isBordered && ::innerBg.isInitialized) {
            innerBg.constrain { color = innerColor }
        }
    }

    companion object {
        fun withImage(image: UIImage, radius: Float = 0f, isBordered: Boolean = false, onClick: () -> Unit = {}) : UIButton {
            return UIButton("", radius, image, 1.0f, isBordered, onClick)
        }
    }
}