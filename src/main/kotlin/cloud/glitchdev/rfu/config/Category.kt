package cloud.glitchdev.rfu.config

import cloud.glitchdev.rfu.RiccioFishingUtils.mc
import cloud.glitchdev.rfu.access.ConfigScreenInvoker
import com.teamresourceful.resourcefulconfigkt.api.CategoryKt
import com.teamresourceful.resourcefulconfigkt.api.ObservableEntry
import com.teamresourceful.resourcefulconfigkt.api.builders.SeparatorBuilder
import com.teamresourceful.resourcefulconfigkt.api.builders.TypeBuilder

/**
 * Extends CategoryKt to add dualSeparator and reloadScreen functionality
 */
open class Category(id: String) : CategoryKt(id) {
    fun reloadableBoolean(
        value: Boolean,
        builder: TypeBuilder.() -> Unit = {}
    ) = ObservableEntry(boolean(value, builder)) { _, _ ->
        reloadScreen()
    }

    fun reloadableBoolean(
        value: Boolean,
        builder: TypeBuilder.() -> Unit = {},
        onChanged: (oldValue: Boolean, newValue: Boolean) -> Unit
    ) = ObservableEntry(boolean(value, builder)) { oldValue, newValue ->
        reloadScreen()
        onChanged(oldValue, newValue)
    }

    fun dualSeparator(builder: SeparatorBuilder.() -> Unit) {
        separator {}
        separator(builder)
    }

    fun reloadScreen() {
        //~ if >=26.2 'mc.screen' -> 'mc.gui.screen()' {
        val screen = mc.gui.screen() as? ConfigScreenInvoker
        //~}
        screen?.`rfu$ReloadAndScroll`()
    }

    fun previewButton(
        previewAction: () -> Unit,
        title: String = "Preview",
        description: String = "Shows a preview of the setting.",
        condition: () -> Boolean = { true }
    ) {
        button {
            this.title = title
            this.description = description
            text = "Preview"
            onClick {
                mc.schedule {
                    //~ if >=26.2 'setScreen' -> 'gui.setScreen' {
                    mc.gui.setScreen(null)
                    //~}
                    previewAction()
                }
            }
            this.condition = condition
        }
    }

    fun customButton(
        onClickAction: () -> Unit,
        title: String,
        description: String = "",
        buttonText: String = "Open",
        condition: () -> Boolean = { true }
    ) {
        button {
            this.title = title
            this.description = description
            text = buttonText
            onClick {
                mc.schedule {
                    onClickAction()
                }
            }
            this.condition = condition
        }
    }
    }