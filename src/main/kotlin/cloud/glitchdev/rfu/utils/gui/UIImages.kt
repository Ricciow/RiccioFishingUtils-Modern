package cloud.glitchdev.rfu.utils.gui

import gg.essential.elementa.components.UIImage
import gg.essential.elementa.utils.ResourceCache

object UIImages {
    private val resourceCache = ResourceCache(50, UIImages::class.java)

    fun ofResource(path: String): UIImage = UIImage.ofResourceCached(path, resourceCache)
}
