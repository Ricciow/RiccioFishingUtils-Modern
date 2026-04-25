package cloud.glitchdev.rfu.utils.dsl

import cloud.glitchdev.rfu.RiccioFishingUtils.MOD_ID
import net.minecraft.resources.Identifier

fun getResource(resource : String) : Identifier {
    val result = Identifier.fromNamespaceAndPath(MOD_ID, resource)
    return result
}

fun parseResource(id :  String) : Identifier? {
    val result = Identifier.tryParse(id)
    return result
}