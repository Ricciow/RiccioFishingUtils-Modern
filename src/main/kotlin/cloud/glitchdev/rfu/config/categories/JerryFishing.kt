package cloud.glitchdev.rfu.config.categories

import cloud.glitchdev.rfu.config.Category
import cloud.glitchdev.rfu.config.TitleSettings
import com.teamresourceful.resourcefulconfig.api.types.options.TranslatableValue

object JerryFishing : Category("Jerry Fishing") {
    override val description: TranslatableValue
        get() = Literal("Settings for Jerry Fishing!")

    init {
        dualSeparator {
            title = "Reindrake"
            description = "Alerts for the Reindrake!"
        }
    }

    val reindrake by obj(TitleSettings(
        title = "&c&l{reindrakes}!",
        duration = 20,
        fadeIn = 10,
        fadeOut = 10,
        placeholders = "{reindrakes}",
        previews = mapOf(
            "Preview Single" to listOf("reindrakes" to "REINDRAKE"),
            "Preview Double" to listOf("reindrakes" to "TWO REINDRAKES")
        )
    )) {
        name = Literal("Reindrake Alert")
        description = Literal("Toggle and edit the title, subtitle, duration, and preview.")
        searchTerms = listOf("reindrake", "title", "subtitle", "duration", "jerry fishing", "double reindrake")
    }

}
