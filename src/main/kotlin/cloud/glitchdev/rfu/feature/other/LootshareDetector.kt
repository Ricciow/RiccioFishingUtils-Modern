package cloud.glitchdev.rfu.feature.other

import cloud.glitchdev.rfu.config.categories.GeneralFishing
import cloud.glitchdev.rfu.events.managers.LootshareEvents.registerLootshareEvent
import cloud.glitchdev.rfu.feature.Feature
import cloud.glitchdev.rfu.feature.RFUFeature
import cloud.glitchdev.rfu.utils.Chat
import cloud.glitchdev.rfu.utils.dsl.toMcCodes
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.HoverEvent

@RFUFeature
object LootshareDetector : Feature {
    override fun onInitialize() {
        registerLootshareEvent { contributors, items ->
            if (!GeneralFishing.lootshareMessage) return@registerLootshareEvent

            val contributorStr = contributors.joinToString("&r, ").toMcCodes()
            val hoverComponent = Component.literal(contributorStr)

            val prefix = Component.literal("&6&lLOOT SHARE! &r".toMcCodes()).withStyle { style ->
                style.withHoverEvent(HoverEvent.ShowText(hoverComponent))
            }

            val finalMessage = Component.empty().append(prefix)

            items.forEachIndexed { index, lootItem ->
                val itemComponent = lootItem.itemStack.hoverName.copy().withStyle { style ->
                    style.withHoverEvent(HoverEvent.ShowItem(lootItem.itemStack))
                }

                val amountSuffix = if (lootItem.diffCount > 1) {
                    Component.literal(" &8${lootItem.diffCount}x".toMcCodes())
                } else {
                    Component.empty()
                }

                finalMessage.append(itemComponent).append(amountSuffix)

                if (index < items.size - 1) {
                    finalMessage.append(Component.literal("&e, ".toMcCodes()))
                }
            }

            Chat.sendMessage(finalMessage)
        }
    }
}