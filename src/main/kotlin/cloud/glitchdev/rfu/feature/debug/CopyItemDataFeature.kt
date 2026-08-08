package cloud.glitchdev.rfu.feature.debug

import cloud.glitchdev.rfu.RiccioFishingUtils.mc
import cloud.glitchdev.rfu.config.categories.DevSettings
import cloud.glitchdev.rfu.constants.text.TextColor
import cloud.glitchdev.rfu.constants.text.TextStyle
import cloud.glitchdev.rfu.events.managers.KeyboardEvents.registerKeyboardEvent
import cloud.glitchdev.rfu.feature.Feature
import cloud.glitchdev.rfu.feature.RFUFeature
import cloud.glitchdev.rfu.mixin.AbstractContainerScreenAccessor
import cloud.glitchdev.rfu.utils.Chat
import cloud.glitchdev.rfu.utils.TextUtils
import com.google.gson.GsonBuilder
import gg.essential.universal.utils.toFormattedString
import gg.essential.universal.utils.toUnformattedString
import net.minecraft.core.component.DataComponents
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.world.item.ItemStack

@RFUFeature
object CopyItemDataFeature : Feature {

    override fun onInitialize() {
        registerKeyboardEvent(
            key = { DevSettings.copyItemDataKeybind },
            onPress = {
                //~ if >=26.2 'screen' -> 'gui.screen()' {
                if (DevSettings.devMode && mc.gui.screen() == null) {
                //~}
                    copyCurrentItemData()
                }
            }
        )
    }

    @JvmStatic
    fun handleContainerKeyPress(key: Int) {
        if (!DevSettings.devMode) return
        val configKey = DevSettings.copyItemDataKeybind
        if (configKey == 0 || key != configKey) return

        copyCurrentItemData()
    }

    fun copyCurrentItemData() {
        //~ if >=26.2 'screen' -> 'gui.screen()' {
        val screen = mc.gui.screen()
        //~}
        val hoveredSlotItem: ItemStack? = (screen as? AbstractContainerScreenAccessor)?.`rfu$getHoveredSlot`()?.item
        val itemStack = if (hoveredSlotItem != null && !hoveredSlotItem.isEmpty) {
            hoveredSlotItem
        } else {
            val player = mc.player
            val main = player?.mainHandItem
            val off = player?.offhandItem
            if (main != null && !main.isEmpty) main
            else if (off != null && !off.isEmpty) off
            else ItemStack.EMPTY
        }

        if (itemStack.isEmpty) {
            Chat.sendMessage(
                TextUtils.rfuLiteral("No item hovered or held to copy data from.", TextStyle(TextColor.LIGHT_RED))
            )
            return
        }

        val dataMap = LinkedHashMap<String, Any?>()

        val customData = itemStack[DataComponents.CUSTOM_DATA]
        val tag = customData?.copyTag()
        val extraAttributes = tag?.getCompound("ExtraAttributes")?.orElse(null)
        val skyblockId = extraAttributes?.getString("id")?.orElse(null)?.takeIf { it.isNotEmpty() }
            ?: tag?.getString("id")?.orElse(null)?.takeIf { it.isNotEmpty() }

        skyblockId?.let { dataMap["skyblockId"] = it }
        dataMap["name"] = itemStack.hoverName.toUnformattedString()
        dataMap["formattedName"] = itemStack.hoverName.toFormattedString()
        dataMap["minecraftId"] = BuiltInRegistries.ITEM.getKey(itemStack.item).toString()
        dataMap["count"] = itemStack.count

        val loreLines = itemStack[DataComponents.LORE]?.lines
        if (!loreLines.isNullOrEmpty()) {
            dataMap["lore"] = loreLines.joinToString("\n") { it.toUnformattedString() }
            dataMap["formattedLore"] = loreLines.joinToString("\n") { it.toFormattedString() }
        }

        if (tag != null && !tag.isEmpty) {
            dataMap["nbt"] = tag.toString()
        }

        val jsonString = GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create().toJson(dataMap)

        mc.keyboardHandler.clipboard = jsonString

        val displayName = itemStack.hoverName.toUnformattedString()
        Chat.sendMessage(
            TextUtils.rfuLiteral("Copied item data for '$displayName' to clipboard!", TextStyle(TextColor.LIGHT_GREEN))
        )
    }
}
