package cloud.glitchdev.rfu.utils.dsl

import cloud.glitchdev.rfu.RiccioFishingUtils.MOD_ID
//? if >=1.21.11 {
import net.minecraft.resources.Identifier
//?} else {
/*import net.minecraft.resources.ResourceLocation
*///?}

//? if >=1.21.11 {
fun getResource(resource : String) : Identifier {
    val result = Identifier.fromNamespaceAndPath(MOD_ID, resource)
//?} else {
/*fun getResource(resource : String) : ResourceLocation {
    val result = ResourceLocation.fromNamespaceAndPath(MOD_ID, resource)
*///?}
    return result
}
//? if >=1.21.11 {
fun parseResource(id :  String) : Identifier? {
    val result = Identifier.tryParse(id)
//?} else {
/*fun parseResource(id :  String) : ResourceLocation? {
    val result = ResourceLocation.tryParse(id)
*///?}
    return result
}