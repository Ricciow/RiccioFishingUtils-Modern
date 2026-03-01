package cloud.glitchdev.rfu.utils.dsl

import gg.essential.universal.utils.toUnformattedString
import net.minecraft.core.component.DataComponents
import net.minecraft.world.item.ItemStack

fun ItemStack.hasDescriptionText(text : String) : Boolean {
    if(this.isEmpty) return false
    val lore = this[DataComponents.LORE] ?: return false

    for (line in lore.lines) {
        val plainText = line.toUnformattedString()

        if(plainText.contains(text)) {
            return true
        }
    }

    return false
}