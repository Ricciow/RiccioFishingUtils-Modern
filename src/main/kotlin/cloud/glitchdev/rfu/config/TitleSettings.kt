package cloud.glitchdev.rfu.config

import cloud.glitchdev.rfu.RiccioFishingUtils.mc
import cloud.glitchdev.rfu.RiccioFishingUtils.settings
import cloud.glitchdev.rfu.access.ConfigScreenInvoker
import cloud.glitchdev.rfu.utils.Title
import cloud.glitchdev.rfu.utils.dsl.formatTemplate
import com.teamresourceful.resourcefulconfigkt.api.ObjectKt
import com.teamresourceful.resourcefulconfigkt.api.ObservableEntry
import com.teamresourceful.resourcefulconfigkt.api.builders.TypeBuilder
import kotlin.math.roundToInt

open class TitleSettings(
    title: String,
    subtitle: String = "",
    duration: Int = 10,
    private val fadeIn: Int = 5,
    private val fadeOut: Int = 5,
    placeholders: String = "None",
    previews: Map<String, List<Pair<String, Any>>> = mapOf("Preview" to emptyList()),
    enabledByDefault: Boolean = true
) : ObjectKt() {
    var enabled by reloadableBoolean(enabledByDefault) {
        name = Literal("Enable Alert")
        description = Literal("Enable this alert and show its settings.")
    }

    init {
        separator {
            this.title = "Appearance"
            condition = { enabled }
        }
    }

    var titleTemplate by string(title) {
        name = Literal("Title")
        description = Literal("Supports & color and formatting codes. Placeholders: $placeholders")
        condition = { enabled }
    }

    var subtitleTemplate by string(subtitle) {
        name = Literal("Subtitle")
        description = Literal("Leave blank for no subtitle. Supports & codes. Placeholders: $placeholders")
        condition = { enabled }
    }

    var durationTicks: Int by transform(
        observable(double("durationSeconds", duration / 20.0) {
            name = Literal("Display Duration (seconds)")
            description = Literal("Time the title stays visible, excluding fades. Rounded to 0.05 seconds (one tick).")
            range = 0.0..30.0
            condition = { enabled }
        }) { _, seconds ->
            val ticks = secondsToTicks(seconds)
            if (seconds != ticks / 20.0) durationTicks = ticks
        },
        { ticks: Int -> ticks.coerceIn(0, 600) / 20.0 },
        { seconds: Double -> secondsToTicks(seconds) }
    )

    init {
        for ((label, values) in previews) {
            button {
                this.title = label
                description = "Shows this title with sample data."
                text = "Preview"
                condition = { enabled }
                onClick {
                    mc.schedule {
                        settings.save()
                        //~ if >=26.2 'setScreen' -> 'gui.setScreen' {
                        mc.gui.setScreen(null)
                        //~}
                        show(*values.toTypedArray())
                    }
                }
            }
        }
    }

    protected fun reloadableBoolean(value: Boolean, builder: TypeBuilder.() -> Unit) =
        ObservableEntry(boolean(value, builder)) { _, _ -> reloadScreen() }

    protected open fun reloadScreen() {
        //~ if >=26.2 'mc.screen' -> 'mc.gui.screen()' {
        val screen = mc.gui.screen() as? ConfigScreenInvoker ?: return
        mc.schedule {
            if (mc.gui.screen() === screen) screen.`rfu$ReloadAndScroll`()
        }
        //~}
    }

    fun show(vararg values: Pair<String, Any>, condition: () -> Boolean = { true }) {
        Title.showTitle(
            titleTemplate.formatTemplate(*values), subtitleTemplate.formatTemplate(*values),
            fadeIn, durationTicks, fadeOut, condition
        )
    }

    companion object {
        fun secondsToTicks(seconds: Double): Int =
            if (seconds.isFinite()) (seconds.coerceIn(0.0, 30.0) * 20).roundToInt() else 0
    }
}
