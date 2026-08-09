package cloud.glitchdev.rfu.events.managers

import cloud.glitchdev.rfu.RiccioFishingUtils.mc
import cloud.glitchdev.rfu.data.EquipmentSet
import cloud.glitchdev.rfu.data.EquipmentSlotType
import cloud.glitchdev.rfu.data.other.OtherManager
import cloud.glitchdev.rfu.data.other.data.StringEntry
import cloud.glitchdev.rfu.events.AutoRegister
import cloud.glitchdev.rfu.events.RegisteredEvent
import cloud.glitchdev.rfu.events.managers.ContainerEvents.registerContainerOpenEvent
import cloud.glitchdev.rfu.events.managers.ItemUsedEvents.registerItemUsedEvent
import cloud.glitchdev.rfu.events.managers.SetSlotEvents.registerSetSlotEvent
import cloud.glitchdev.rfu.events.managers.SlotClickedEvents.registerSlotClicked
import cloud.glitchdev.rfu.utils.Coroutines
import gg.essential.universal.utils.toUnformattedString
import kotlinx.coroutines.Job
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.core.component.DataComponents
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.world.item.ItemStack

@AutoRegister
object EquipmentTracker : RegisteredEvent {

    private var debounceJob: Job? = null
    private const val DEBOUNCE_DELAY_MS = 100L

    override fun register() {
        loadInitialEquipment()

        registerContainerOpenEvent { containerName, _, items ->
            scheduleDebouncedCheck { handleContainerUpdate(containerName, items) }
        }

        registerSetSlotEvent { _, _, _ ->
            scheduleDebouncedCheck {
                //~ if >=26.2 'mc.screen' -> 'mc.gui.screen()' {
                val screen = mc.gui.screen() ?: return@scheduleDebouncedCheck
                //~}
                val title = screen.title.toUnformattedString()
                val player = mc.player ?: return@scheduleDebouncedCheck
                val items = player.containerMenu.slots.map { it.item }
                handleContainerUpdate(title, items)
            }
        }

        registerSlotClicked { slot, title ->
            if (title.contains("Equipment Sets")) {
                if (slot.index in 36..44) {
                    val name = slot.item.hoverName.toUnformattedString()
                    if ("Equipped" in name) {
                        updateIfChanged(EquipmentSet())
                    }
                }
            }
        }

        registerItemUsedEvent { item ->
            handleItemUsed(item)
        }
    }

    private fun scheduleDebouncedCheck(block: () -> Unit) {
        if (debounceJob != null) return
        debounceJob = Coroutines.setTimeout(DEBOUNCE_DELAY_MS) {
            try {
                block()
            } finally {
                debounceJob = null
            }
        }
    }

    private fun loadInitialEquipment() {
        val necklace = (OtherManager.getField("equipment_necklace") as? StringEntry)?.value ?: ""
        val cloak = (OtherManager.getField("equipment_cloak") as? StringEntry)?.value ?: ""
        val belt = (OtherManager.getField("equipment_belt") as? StringEntry)?.value ?: ""
        val gloves = (OtherManager.getField("equipment_gloves") as? StringEntry)?.value ?: ""

        EquipmentEvents.EquipmentChangeEventManager.updateEquipmentSet(
            EquipmentSet(necklace, cloak, belt, gloves),
            forceNotify = true
        )
    }

    private fun handleContainerUpdate(containerName: String, items: List<ItemStack>) {
        when {
            containerName.contains("Equipment Sets") -> parseEquipmentSetsContainer(items)
            containerName.contains("Loadouts") || containerName.contains("Stats & Equipment") -> parseDirectEquipmentContainer(items)
        }
    }

    private fun parseDirectEquipmentContainer(items: List<ItemStack>) {
        val newSet = EquipmentSet(
            necklace = getItemName(items.getOrNull(10)),
            cloak = getItemName(items.getOrNull(19)),
            belt = getItemName(items.getOrNull(28)),
            gloves = getItemName(items.getOrNull(37))
        )
        updateIfChanged(newSet)
    }

    private fun parseEquipmentSetsContainer(items: List<ItemStack>) {
        for (slot in 36..44) {
            val item = items.getOrNull(slot) ?: continue
            val name = item.hoverName.toUnformattedString()

            if ("Equipped" in name) {
                val newSet = EquipmentSet(
                    necklace = getItemName(items.getOrNull(slot - 36)),
                    cloak = getItemName(items.getOrNull(slot - 27)),
                    belt = getItemName(items.getOrNull(slot - 18)),
                    gloves = getItemName(items.getOrNull(slot - 9))
                )
                updateIfChanged(newSet)
                return
            }
        }
    }

    private fun handleItemUsed(item: ItemStack) {
        if (item.isEmpty) return
        val lore = item[DataComponents.LORE]?.lines?.map { it.toUnformattedString() } ?: return

        val isEquipable = lore.any { line ->
            val lower = line.lowercase()
            lower.contains("right-click to equip") || lower.contains("click to equip")
        }
        if (!isEquipable) return

        val slotType = detectSlotTypeFromLore(lore) ?: return
        val current = EquipmentEvents.currentEquipmentSet

        val currentSlotEquipped = when (slotType) {
            EquipmentSlotType.NECKLACE -> current.necklace
            EquipmentSlotType.CLOAK -> current.cloak
            EquipmentSlotType.BELT -> current.belt
            EquipmentSlotType.GLOVES -> current.gloves
        }
        if (currentSlotEquipped.isNotEmpty()) return

        val itemName = item.hoverName.toUnformattedString()
        val newSet = when (slotType) {
            EquipmentSlotType.NECKLACE -> current.copy(necklace = itemName)
            EquipmentSlotType.CLOAK -> current.copy(cloak = itemName)
            EquipmentSlotType.BELT -> current.copy(belt = itemName)
            EquipmentSlotType.GLOVES -> current.copy(gloves = itemName)
        }
        updateIfChanged(newSet)
    }

    private fun detectSlotTypeFromLore(lore: List<String>): EquipmentSlotType? {
        for (line in lore.asReversed()) {
            val uppercaseLine = line.uppercase()
            when {
                uppercaseLine.contains("NECKLACE") -> return EquipmentSlotType.NECKLACE
                uppercaseLine.contains("CLOAK") -> return EquipmentSlotType.CLOAK
                uppercaseLine.contains("BELT") -> return EquipmentSlotType.BELT
                uppercaseLine.contains("GLOVES") || uppercaseLine.contains("BRACELET") -> return EquipmentSlotType.GLOVES
            }
        }
        return null
    }

    private fun updateIfChanged(newSet: EquipmentSet) {
        if (EquipmentEvents.currentEquipmentSet.hasChanged(newSet)) {
            OtherManager.setField("equipment_necklace", StringEntry(newSet.necklace))
            OtherManager.setField("equipment_cloak", StringEntry(newSet.cloak))
            OtherManager.setField("equipment_belt", StringEntry(newSet.belt))
            OtherManager.setField("equipment_gloves", StringEntry(newSet.gloves))
            OtherManager.file.save()
            println("Changed ${newSet.necklace} | ${newSet.cloak} | ${newSet.belt} | ${newSet.gloves}")
            EquipmentEvents.EquipmentChangeEventManager.updateEquipmentSet(newSet)
        }
    }

    private fun getItemName(item: ItemStack?): String {
        if (item == null || item.isEmpty || isStainedGlassPane(item)) return ""
        val name = item.hoverName.toUnformattedString()
        if (name.endsWith("Ready") || name.endsWith("Empty") || name == "Close" || name.startsWith("SkyBlock Menu")) return ""
        return name
    }

    private fun isStainedGlassPane(itemStack: ItemStack): Boolean {
        if (itemStack.isEmpty) return false
        val id = BuiltInRegistries.ITEM.getKey(itemStack.item).toString()
        return id.endsWith("stained_glass_pane")
    }
}
