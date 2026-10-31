package cloud.glitchdev.rfu.config.configList

import com.teamresourceful.resourcefulconfig.api.annotations.ConfigOption
import com.teamresourceful.resourcefulconfig.api.types.options.EntryData
import com.teamresourceful.resourcefulconfig.api.types.options.Option
import com.teamresourceful.resourcefulconfig.api.types.options.TranslatableValue
import net.minecraft.resources.Identifier

class ConfigListBuilder(val id: String) {
    var name = TranslatableValue(id)
    var description = TranslatableValue.EMPTY
    var condition: () -> Boolean = { true }
    var hidden = false
    var renderer: Identifier? = null
    var searchTerms: List<String> = emptyList()
    private val options = mutableMapOf<Option<*, *>, Any>()

    fun <A : Annotation, D : Any> option(option: Option<A, D>, value: D) {
        options[option] = value
    }

    internal fun entryData(baseTranslation: String): EntryData {
        fun TranslatableValue.withBase(): TranslatableValue =
            if (baseTranslation.isEmpty() || translation().isEmpty()) this
            else TranslatableValue(value(), "$baseTranslation.${translation()}")

        val dataOptions = options.toMutableMap()
        if (hidden) dataOptions[Option.HIDDEN] = ConfigOption.Hidden()
        renderer?.let { dataOptions[Option.RENDERER] = it }
        dataOptions[Option.SEARCH_TERM] = searchTerms
        return EntryData(name.withBase(), description.withBase(), dataOptions)
    }
}
