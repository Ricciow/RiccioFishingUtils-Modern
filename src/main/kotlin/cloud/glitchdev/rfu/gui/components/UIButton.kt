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
import gg.essential.elementa.dsl.percent
import gg.essential.elementa.dsl.pixels
import gg.essential.elementa.dsl.toConstraint
import gg.essential.universal.UMatrixStack

/**
 * Simple Button Component
 */
class UIButton(val text: String, radius: Float = 0f, val image : UIImage? = null, val baseTextScale: Float = 1.0f, var onClick : () -> Unit = {}) : UIRoundedRectangle(radius) {
    val primaryColor = UIScheme.secondaryColorOpaque.toConstraint()
    val hoverColor = UIScheme.secondaryColor.toConstraint()
    val textColor = UIScheme.primaryTextColor.toConstraint()
    val secondaryTextColor = UIScheme.secondaryTextColor.toConstraint()
    val disabledColor = UIScheme.secondaryColorDisabledOpaque.toConstraint()
    val hoverDuration = UIScheme.HOVER_EFFECT_DURATION
    val clickDuration = 0.1f

    var disabled = false
        set(value) {
            field = value
            this.constrain {
                color = if (disabled) disabledColor else primaryColor
            }
            textArea.constrain {
                color = if (disabled) secondaryTextColor else textColor
            }
            if(image != null) {
                image.constrain {
                    color = if (disabled) secondaryTextColor else textColor
                }
            }
        }

    lateinit var textArea : UIText
    private var lastWidth = -1f
    private var lastHeight = -1f

    init {
        create()
    }

    fun setText(text : String) {
        textArea.setText(text)
        updateFontSize()
    }

    fun create() {
        this.constrain {
            color = primaryColor
        }

        val initialHeight = 9f * baseTextScale
        textArea = UIText(text).constrain {
            x = CenterConstraint()
            y = CenterConstraint()
            // Will be updated by updateFontSize
            height = initialHeight.pixels()
            width = TextAspectConstraint()
            color = textColor
        } childOf this

        if(image != null) {
            textArea.setHidden(true)
            image.constrain {
                x = CenterConstraint()
                y = CenterConstraint()
                width = 70.percent()
                height = 70.percent()
                color = textColor
            } childOf this
        }

        this.onMouseClick {
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
                textArea.animate {
                    setColorAnimation(Animations.IN_EXP, clickDuration, textColor)
                }
                image?.animate {
                    setColorAnimation(Animations.IN_EXP, clickDuration, textColor)
                }
            }
        }
        .onMouseEnter {
            if(!disabled) {
                this.animate {
                    setColorAnimation(Animations.IN_EXP, hoverDuration, hoverColor)
                }
            }
        }
        .onMouseLeave {
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
    
    companion object {
        fun withImage(image: UIImage, radius: Float = 0f, onClick: () -> Unit = {}) : UIButton {
            return UIButton("", radius, image, 1.0f, onClick)
        }
    }
}