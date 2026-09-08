package cloud.glitchdev.rfu.gui.components.hud

import cloud.glitchdev.rfu.RiccioFishingUtils.mc
import cloud.glitchdev.rfu.utils.RFULogger
import gg.essential.elementa.UIComponent
import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.components.UIImage
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.constraints.ConstraintType
import gg.essential.elementa.constraints.PositionConstraint
import gg.essential.elementa.constraints.resolution.ConstraintVisitor
import gg.essential.elementa.dsl.childOf
import gg.essential.elementa.dsl.constrain
import gg.essential.elementa.dsl.pixels
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import java.awt.image.BufferedImage
import java.io.InputStream
import java.util.concurrent.CompletableFuture
import javax.imageio.ImageIO

class UIFakeInventory : UIContainer() {
    companion object {
        const val INVENTORY_WIDTH = 176f
        const val INVENTORY_HEIGHT = 166f
        private const val INVENTORY_TEXTURE_PATH = "/assets/minecraft/textures/gui/container/inventory.png"

        fun getInventoryLeft(windowWidth: Float): Float {
            return ((windowWidth.toInt() - INVENTORY_WIDTH.toInt()) / 2).toFloat()
        }

        fun getInventoryTop(windowHeight: Float): Float {
            return ((windowHeight.toInt() - INVENTORY_HEIGHT.toInt()) / 2).toFloat()
        }

        fun loadInventoryImage(): BufferedImage? {
            return try {
                val stream: InputStream? = try {
                    val resourceOpt = mc.resourceManager.getResource(AbstractContainerScreen.INVENTORY_LOCATION)
                    if (resourceOpt.isPresent) resourceOpt.get().open() else null
                } catch (e: Exception) {
                    null
                } ?: UIFakeInventory::class.java.getResourceAsStream(INVENTORY_TEXTURE_PATH)
                  ?: Thread.currentThread().contextClassLoader?.getResourceAsStream("assets/minecraft/textures/gui/container/inventory.png")

                stream?.use { s ->
                    val fullImage = ImageIO.read(s)
                    if (fullImage != null && fullImage.width > 0 && fullImage.height > 0) {
                        val cropW = (176.0 / 256.0 * fullImage.width).toInt().coerceIn(1, fullImage.width)
                        val cropH = (166.0 / 256.0 * fullImage.height).toInt().coerceIn(1, fullImage.height)
                        fullImage.getSubimage(0, 0, cropW, cropH)
                    } else {
                        fullImage
                    }
                }
            } catch (e: Exception) {
                RFULogger.error("Failed to load inventory texture from resource manager / pack", e)
                null
            }
        }
    }

    private var imageComponent: UIImage? = null

    init {
        this.constrain {
            x = InventoryPositionConstraint(isX = true)
            y = InventoryPositionConstraint(isX = false)
            width = INVENTORY_WIDTH.pixels()
            height = INVENTORY_HEIGHT.pixels()
        }

        refreshTexture()
    }

    fun refreshTexture() {
        imageComponent?.let { removeChild(it) }
        val image = loadInventoryImage()
        if (image != null) {
            imageComponent = UIImage(CompletableFuture.completedFuture(image)).constrain {
                x = CenterConstraint()
                y = CenterConstraint()
                width = INVENTORY_WIDTH.pixels()
                height = INVENTORY_HEIGHT.pixels()
            } childOf this
        }
    }

    private class InventoryPositionConstraint(private val isX: Boolean) : PositionConstraint {
        override var cachedValue = 0f
        override var recalculate = true
        override var constrainTo: UIComponent? = null

        override fun getXPositionImpl(component: UIComponent): Float {
            val parent = constrainTo ?: component.parent
            return getInventoryLeft(parent.getWidth())
        }

        override fun getYPositionImpl(component: UIComponent): Float {
            val parent = constrainTo ?: component.parent
            return getInventoryTop(parent.getHeight())
        }

        override fun visitImpl(visitor: ConstraintVisitor, type: ConstraintType) {
            if (isX) {
                visitor.visitParent(ConstraintType.WIDTH)
                visitor.visitParent(ConstraintType.X)
            } else {
                visitor.visitParent(ConstraintType.HEIGHT)
                visitor.visitParent(ConstraintType.Y)
            }
        }
    }
}

