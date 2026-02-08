package cloud.glitchdev.rfu.config

import cloud.glitchdev.rfu.RiccioFishingUtils.mc
import cloud.glitchdev.rfu.access.ConfigScreenInvoker
import com.teamresourceful.resourcefulconfigkt.api.CategoryKt
import com.teamresourceful.resourcefulconfigkt.api.builders.SeparatorBuilder

/**
 * Extends CategoryKt to add dualSeparator and reloadScreen functionality
 */
open class Category(id : String) : CategoryKt(id) {
    fun dualSeparator(builder: SeparatorBuilder.() -> Unit) {
        separator {}
        separator(builder)
    }

    fun reloadScreen() {
        val screen = mc.screen as? ConfigScreenInvoker
        screen?.`rfu$ReloadAndScroll`()
    }
}