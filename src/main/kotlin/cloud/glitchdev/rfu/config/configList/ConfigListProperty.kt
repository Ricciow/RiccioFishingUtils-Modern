package cloud.glitchdev.rfu.config.configList

import com.teamresourceful.resourcefulconfig.api.annotations.ConfigEntry
import com.teamresourceful.resourcefulconfig.api.types.elements.ResourcefulConfigListEntryElement
import com.teamresourceful.resourcefulconfig.api.types.entries.ResourcefulConfigEntry
import com.teamresourceful.resourcefulconfig.api.types.entries.ResourcefulConfigListEntry
import com.teamresourceful.resourcefulconfig.api.types.options.EntryType
import com.teamresourceful.resourcefulconfig.common.loader.entries.ParsedListEntry
import com.teamresourceful.resourcefulconfig.common.loader.entries.ParsedListItemValueEntry
import com.teamresourceful.resourcefulconfigkt.api.ConfigDelegateProvider
import com.teamresourceful.resourcefulconfigkt.api.ObjectKt
import com.teamresourceful.resourcefulconfigkt.api.builders.EntriesBuilder
import java.lang.reflect.Modifier
import kotlin.reflect.KProperty

class ConfigListProperty<T : Any>(
    private val elementClass: Class<T>,
    private val defaults: List<T>,
    private val id: String?,
    private val builder: ConfigListBuilder.() -> Unit
) : ConfigDelegateProvider<Lazy<MutableList<T>>> {
    override operator fun provideDelegate(entries: EntriesBuilder, prop: KProperty<*>): Lazy<MutableList<T>> {
        val entryId = id ?: prop.name
        require(!List::class.java.isAssignableFrom(elementClass) && !elementClass.isArray) {
            "Config list $entryId cannot contain lists or arrays."
        }
        require(!ObjectKt::class.java.isAssignableFrom(elementClass)) {
            "Config list $entryId requires @ConfigEntry fields instead of ObjectKt delegates."
        }
        val elementType = requireNotNull(EntryType.entries.firstOrNull {
            it != EntryType.LIST && it.test(elementClass)
        }) {
            "Config list $entryId does not support ${elementClass.name}; use a scalar, enum or @ConfigObject class."
        }
        if (elementType == EntryType.OBJECT) {
            require(Modifier.isPublic(elementClass.modifiers) &&
                elementClass.constructors.any { it.parameterCount == 0 }) {
                "Config list $entryId requires a public element class with a public no-argument constructor."
            }
        }

        val settings = ConfigListBuilder(entryId).apply(builder)
        val data = settings.entryData(entries.baseTranslation)
        val values = defaults.toMutableList()
        // The core parser uses this field for annotations and snapshots, not storage.
        val parsed = ParsedListEntry(Backing::class.java.getField("values"), elementClass, values)
        val listEntry = object : ResourcefulConfigListEntry by parsed {
            override fun options() = data

            override fun get(index: Int): ResourcefulConfigEntry {
                val item = parsed.get(index)
                return if (item is ParsedListItemValueEntry) {
                    ParsedListItemValueEntry(
                        item.type(), data, elementClass, item.list(), item.index(), item.defaultValue()
                    )
                } else item
            }
        }
        entries.element(object : ResourcefulConfigListEntryElement {
            override fun id() = entryId
            override fun entry() = listEntry
            override fun isHidden() = !settings.condition() || super.isHidden()
        })

        return lazyOf(values)
    }

    private class Backing {
        @ConfigEntry(id = "values")
        @JvmField
        val values = mutableListOf<Any>()
    }
}
