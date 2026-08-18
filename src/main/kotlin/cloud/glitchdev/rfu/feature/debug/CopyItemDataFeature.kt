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
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.core.component.DataComponents
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.world.entity.player.Inventory
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

        registerKeyboardEvent(
            key = { DevSettings.copyContainerDataKeybind },
            onPress = {
                //~ if >=26.2 'screen' -> 'gui.screen()' {
                if (DevSettings.devMode && mc.gui.screen() == null) {
                //~}
                    copyContainerData()
                }
            }
        )
    }

    @JvmStatic
    fun handleContainerKeyPress(key: Int) {
        if (!DevSettings.devMode || key == 0) return
        if (key == DevSettings.copyItemDataKeybind) {
            copyCurrentItemData()
        } else if (key == DevSettings.copyContainerDataKeybind) {
            copyContainerData()
        }
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
        extractItemData(itemStack, dataMap)

        val jsonString = GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create().toJson(dataMap)
        mc.keyboardHandler.clipboard = jsonString

        val displayName = itemStack.hoverName.toUnformattedString()
        Chat.sendMessage(
            TextUtils.rfuLiteral("Copied item data for '$displayName' to clipboard!", TextStyle(TextColor.LIGHT_GREEN))
        )
    }

    fun copyContainerData() {
        //~ if >=26.2 'screen' -> 'gui.screen()' {
        val screen = mc.gui.screen() as? AbstractContainerScreen<*>
        //~}
        if (screen == null) {
            Chat.sendMessage(
                TextUtils.rfuLiteral("No container screen open to copy data from.", TextStyle(TextColor.LIGHT_RED))
            )
            return
        }

        val title = screen.title.toUnformattedString()
        val formattedTitle = screen.title.toFormattedString()
        val containerSlots = screen.menu.slots.filter { it.container !is Inventory && it.container != mc.player?.inventory }

        val itemList = mutableListOf<Map<String, Any?>>()
        var itemCount = 0

        for (slot in containerSlots) {
            val itemStack = slot.item
            if (itemStack.isEmpty || isStainedGlassPane(itemStack)) continue
            itemCount++

            val itemData = LinkedHashMap<String, Any?>()
            itemData["slot"] = slot.index
            extractItemData(itemStack, itemData)
            itemList.add(itemData)
        }

        val containerData = LinkedHashMap<String, Any?>()
        containerData["containerTitle"] = title
        containerData["formattedTitle"] = formattedTitle
        containerData["totalSlots"] = containerSlots.size
        containerData["itemCount"] = itemCount
        containerData["items"] = itemList

        val jsonString = GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create().toJson(containerData)
        mc.keyboardHandler.clipboard = jsonString

        Chat.sendMessage(
            TextUtils.rfuLiteral("Copied container data ($itemCount items) for '$title' to clipboard!", TextStyle(TextColor.LIGHT_GREEN))
        )
    }

    private fun extractItemData(itemStack: ItemStack, dataMap: MutableMap<String, Any?>) {
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
    }

    private fun isStainedGlassPane(itemStack: ItemStack): Boolean {
        if (itemStack.isEmpty) return false
        val id = BuiltInRegistries.ITEM.getKey(itemStack.item).toString()
        return id.endsWith("_stained_glass_pane") || id == "minecraft:stained_glass_pane"
    }
}
