package cloud.glitchdev.rfu.feature.fishing

import cloud.glitchdev.rfu.config.categories.SeaCreatureConfig
import cloud.glitchdev.rfu.constants.SeaCreatures
import cloud.glitchdev.rfu.events.managers.ChatEvents.registerAllowGameEvent
import cloud.glitchdev.rfu.events.managers.SeaCreatureCatchEvents
import cloud.glitchdev.rfu.events.managers.SeaCreatureCatchEvents.registerSeaCreatureCatchEvent
import cloud.glitchdev.rfu.feature.Feature
import cloud.glitchdev.rfu.feature.RFUFeature
import cloud.glitchdev.rfu.utils.Chat
import cloud.glitchdev.rfu.utils.dsl.toMcCodes
import net.minecraft.network.chat.Component

@RFUFeature
object CatchMessageReplacer : Feature {
    override fun onInitialize() {
        registerAllowGameEvent(SeaCreatureCatchEvents.SC_MESSAGE_REGEX) { _, _, _ ->
            !SeaCreatureConfig.replaceCatchMessages
        }

        registerAllowGameEvent(SeaCreatureCatchEvents.DOUBLE_HOOK_REGEX) { _, _, _ ->
            !SeaCreatureConfig.replaceCatchMessages
        }

        registerSeaCreatureCatchEvent { sc, isDoubleHook, _, _, _ ->
            if (SeaCreatureConfig.replaceCatchMessages) {

                val formattedMessage = getTemplate(isDoubleHook)
                    .replace("{article}", sc.getArticle())
                    .replace("{article_upper}", sc.getArticle().replaceFirstChar { it.uppercaseChar() })
                    .replace("{name}", sc.getNameWithoutArticle())
                    .replace("{style}", sc.getStyleCode())
                    .replace("{plural}", sc.getPluralName())
                    .replace("{mob}", sc.getSingularNameWithArticle())
                    .replace("{mobs}", sc.getPluralName())
                    .toMcCodes()

                Chat.sendMessage(Component.literal(formattedMessage))
            }
        }
    }

    fun preview() {
        val sc = SeaCreatures.entries.random()
        val isDoubleHook = (0..1).random() == 1

        val formattedMessage = getTemplate(isDoubleHook)
            .replace("{article}", sc.getArticle())
            .replace("{article_upper}", sc.getArticle().replaceFirstChar { it.uppercaseChar() })
            .replace("{name}", sc.getNameWithoutArticle())
            .replace("{style}", sc.getStyleCode())
            .replace("{plural}", sc.getPluralName())
            .replace("{mob}", sc.getSingularNameWithArticle())
            .replace("{mobs}", sc.getPluralName())
            .toMcCodes()

        Chat.sendMessage(Component.literal(formattedMessage))
    }

    private fun getTemplate(isDoubleHook : Boolean) : String {
        return if (isDoubleHook) {
            SeaCreatureConfig.doubleHookCatchMessageTemplate
        } else {
            SeaCreatureConfig.catchMessageTemplate
        }
    }
}
