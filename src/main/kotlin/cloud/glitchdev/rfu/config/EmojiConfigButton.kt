package cloud.glitchdev.rfu.config

import cloud.glitchdev.rfu.constants.text.EmojiData
import com.teamresourceful.resourcefulconfig.api.client.ResourcefulConfigElementRenderer
import com.teamresourceful.resourcefulconfig.api.client.ResourcefulConfigUI
import com.teamresourceful.resourcefulconfig.api.types.ResourcefulConfigButton
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.network.chat.Component
import net.minecraft.resources.Identifier

/** ResourcefulConfig's string button titles cannot carry a native sprite component. */
class EmojiConfigButton(
    private val emoji: EmojiData,
    private val title: String,
    private val description: String,
    private val text: String,
    private val onClick: () -> Unit,
) : ResourcefulConfigButton {
    override fun title() = title
    override fun description() = description
    override fun text() = text
    override fun invoke() = runCatching(onClick).isSuccess
    override fun renderer() = RENDERER

    companion object {
        private val RENDERER = Identifier.fromNamespaceAndPath("rfu", "emoji_button").also { id ->
            ResourcefulConfigUI.registerElementRenderer(id) { element ->
                val button = element as EmojiConfigButton
                object : ResourcefulConfigElementRenderer {
                    override fun title(): Component = button.emoji.component().append(" ${button.title}")
                    override fun description(): Component = Component.literal(button.description)
                    override fun widgets(): List<AbstractWidget> = listOf(
                        ResourcefulConfigUI.button(0, 0, 96, 12, Component.literal(button.text)) { button.invoke() }
                    )
                }
            }
        }
    }
}
