package cloud.glitchdev.rfu.gui.window

import cloud.glitchdev.rfu.RiccioFishingUtils.mc
import cloud.glitchdev.rfu.constants.text.Emoji
import cloud.glitchdev.rfu.constants.text.EmojiData
import cloud.glitchdev.rfu.data.other.EmojiPreferences
import cloud.glitchdev.rfu.gui.UIScheme
import cloud.glitchdev.rfu.gui.components.checkbox.UICheckbox
import cloud.glitchdev.rfu.gui.components.elementa.UIEmoji
import cloud.glitchdev.rfu.gui.components.elementa.UIVanillaTooltip
import cloud.glitchdev.rfu.gui.components.textinput.UIDecoratedTextInput
import gg.essential.elementa.components.ScrollComponent
import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.components.UIRoundedRectangle
import gg.essential.elementa.components.UIText
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.constraints.CramSiblingConstraint
import gg.essential.elementa.constraints.FillConstraint
import gg.essential.elementa.constraints.SiblingConstraint
import gg.essential.elementa.constraints.TextAspectConstraint
import gg.essential.elementa.dsl.childOf
import gg.essential.elementa.dsl.constrain
import gg.essential.elementa.dsl.min
import gg.essential.elementa.dsl.minus
import gg.essential.elementa.dsl.percent
import gg.essential.elementa.dsl.pixels
import gg.essential.elementa.dsl.toConstraint
import net.minecraft.ChatFormatting
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component

class EmojiEditWindow(private val parentScreen: Screen? = null) : BaseWindow(true) {
    private companion object {
        const val SEARCH_RESULT_LIMIT = 150
    }

    private val catalog = Emoji.EMOJIS.sortedBy { it.displayName.lowercase() }
    private val categories = catalog.groupBy { it.category }
        .toSortedMap(compareBy<String>(Emoji::formatDisplayName).thenBy { it })
    private val labels = mutableMapOf<String, UIText>()
    private var selected: String? = null
    private val searchBar: UIDecoratedTextInput
    private val searchStatus: UIText
    private val emojiList: ScrollComponent

    init {
        val background = UIRoundedRectangle(5f).constrain {
            width = 100.percent()
            height = 100.percent()
            color = UIScheme.windowBackground.toConstraint()
        } childOf window

        val sidebarWidth = min(31.percent(), 130.pixels())
        val sidebar = UIRoundedRectangle(2f).constrain {
            x = 5.pixels()
            y = 5.pixels()
            width = sidebarWidth
            height = 100.percent() - 10.pixels()
            color = UIScheme.sidebarBackground.toConstraint()
        } childOf background

        val categoryList = ScrollComponent(verticalScrollEnabled = true).constrain {
            x = 3.pixels()
            y = 3.pixels()
            width = 100.percent() - 9.pixels()
            height = 100.percent() - 6.pixels()
        } childOf sidebar

        val categoryScrollbar = UIRoundedRectangle(2f).constrain {
            x = 2.pixels(true)
            width = 3.pixels()
            height = 100.percent()
            color = UIScheme.secondaryColor.toConstraint()
        } childOf sidebar
        categoryList.setVerticalScrollBarComponent(categoryScrollbar, hideWhenUseless = true)

        for (category in categories.keys) {
            val row = UIContainer().constrain {
                y = SiblingConstraint()
                width = 100.percent()
                height = 22.pixels()
            }.onMouseClick { selectCategory(category) } childOf categoryList

            labels[category] = UIText(Emoji.formatDisplayName(category)).constrain {
                x = 7.pixels()
                y = CenterConstraint()
                width = min(TextAspectConstraint(), 100.percent() - 9.pixels())
                color = UIScheme.primaryTextColor.toConstraint()
            } childOf row
        }

        val content = UIContainer().constrain {
            x = SiblingConstraint(5f)
            y = 5.pixels()
            width = 100.percent() - sidebarWidth - 15.pixels()
            height = 100.percent() - 10.pixels()
        } childOf background

        val header = UIContainer().constrain {
            width = 100.percent()
            height = 23.pixels()
        } childOf content

        UIText("Edit Emojis").constrain {
            x = 5.pixels()
            y = CenterConstraint()
            color = UIScheme.primaryTextColor.toConstraint()
        } childOf header

        searchBar = UIDecoratedTextInput("Search...", 2f).constrain {
            y = SiblingConstraint()
            width = 100.percent()
            height = 18.pixels()
        } childOf content

        searchStatus = UIText("").constrain {
            x = 5.pixels()
            y = SiblingConstraint()
            width = min(TextAspectConstraint(), 100.percent() - 10.pixels())
            height = 0.pixels()
            textScale = 0.8.pixels()
            color = UIScheme.secondaryTextColor.toConstraint()
        } childOf content

        val emojiArea = UIContainer().constrain {
            y = SiblingConstraint()
            width = 100.percent()
            height = FillConstraint()
        } childOf content

        emojiList = ScrollComponent(verticalScrollEnabled = true).constrain {
            width = 100.percent() - 6.pixels()
            height = 100.percent()
        } childOf emojiArea
        val scrollbar = UIRoundedRectangle(2f).constrain {
            x = 0.pixels(true)
            width = 3.pixels()
            height = 100.percent()
            color = UIScheme.secondaryColor.toConstraint()
        } childOf emojiArea
        emojiList.setVerticalScrollBarComponent(scrollbar, hideWhenUseless = true)
        searchBar.onChange = ::refreshEmojis

        (categories.keys.firstOrNull { it == "general" } ?: categories.keys.firstOrNull())?.let(::selectCategory)
    }

    private fun selectCategory(category: String) {
        if (category == selected && searchBar.getText().isEmpty()) return
        selected?.let(labels::get)?.constrain { color = UIScheme.primaryTextColor.toConstraint() }
        labels.getValue(category).constrain { color = UIScheme.selectedTextColor.toConstraint() }
        selected = category

        searchBar.setText("")
        refreshEmojis("")
    }

    private fun refreshEmojis(text: String) {
        val query = text.trim().trim(':').trim()
        val matches = if (query.isEmpty()) {
            categories[selected].orEmpty()
        } else {
            catalog.mapNotNull { emoji -> searchRank(emoji, query)?.let { it to emoji } }
                .sortedBy { it.first }
                .map { it.second }
        }
        val status = when {
            query.isEmpty() -> ""
            matches.isEmpty() -> "No emojis found"
            matches.size > SEARCH_RESULT_LIMIT ->
                "Showing $SEARCH_RESULT_LIMIT of ${matches.size} matches — refine search"
            else -> ""
        }
        searchStatus.setText(status)
        searchStatus.constrain { height = if (status.isEmpty()) 0.pixels() else 13.pixels() }

        emojiList.clearChildren()
        emojiList.scrollToTop()
        val visible = if (query.isEmpty()) matches else matches.take(SEARCH_RESULT_LIMIT)
        visible.forEach { addEmoji(emojiList, it) }
    }

    private fun searchRank(emoji: EmojiData, query: String): Int? {
        val shortcuts = emoji.aliases + emoji.customTriggers.map { it.trim(':') }
        if (shortcuts.any { it.equals(query, ignoreCase = true) }) return 0

        val fields = shortcuts + listOf(emoji.displayName, emoji.category, Emoji.formatDisplayName(emoji.category))
        return when {
            fields.any { it.startsWith(query, ignoreCase = true) } -> 1
            fields.any { it.contains(query, ignoreCase = true) } -> 2
            else -> null
        }
    }

    private fun addEmoji(list: ScrollComponent, emoji: EmojiData) {
        val card = UIRoundedRectangle(3f).constrain {
            x = CramSiblingConstraint(4f)
            y = CramSiblingConstraint(4f)
            width = min(96.pixels(), 100.percent())
            height = 35.pixels()
            color = UIScheme.contentBackground.toConstraint()
        } childOf list

        val tooltip = UIVanillaTooltip(buildList {
            add(Component.literal(emoji.displayName).withStyle(ChatFormatting.YELLOW))
            for (trigger in emoji.triggers) {
                // Split the shortcut across components so the emoji renderer leaves it readable.
                add(Component.literal(trigger.take(1)).append(trigger.drop(1)).withStyle(ChatFormatting.GRAY))
            }
        }).constrain {
            width = 100.percent()
            height = 100.percent()
        } childOf card

        val shortcut = emoji.customTriggers.firstOrNull().takeIf { emoji.messageReplacement != null }
        val name = shortcut?.let { "${emoji.displayName} ($it)" } ?: emoji.displayName
        val checkbox = UICheckbox(name, EmojiPreferences.isEnabled(emoji)) {
            EmojiPreferences.setEnabled(emoji, it)
            //~ if >=26.2 'gui.chat' -> 'gui.hud.chat' {
            mc.gui.hud.chat.rescaleChat()
            //~}
        }.constrain {
            width = 100.percent()
            height = 100.percent()
        } childOf tooltip

        checkbox.checkbox.constrain {
            x = 5.pixels()
            y = 5.pixels()
        }
        checkbox.textComponent.constrain {
            x = CenterConstraint()
            y = 22.pixels()
            width = min(TextAspectConstraint(), 100.percent() - 8.pixels())
        }

        val preview = if (emoji.font != null) UIEmoji(emoji) else UIText(emoji.text)
        preview.constrain {
            x = CenterConstraint()
            y = 8.pixels()
        } childOf checkbox
    }

    override fun onWindowClose() {
        displayScreen(parentScreen)
    }
}
