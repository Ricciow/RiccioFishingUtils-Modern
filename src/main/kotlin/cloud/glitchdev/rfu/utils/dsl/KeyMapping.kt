package cloud.glitchdev.rfu.utils.dsl

import cloud.glitchdev.rfu.mixin.KeyMappingAccessor
import com.mojang.blaze3d.platform.InputConstants
import net.minecraft.client.KeyMapping

val KeyMapping.rfuKey: InputConstants.Key
    get() = (this as KeyMappingAccessor).`rfu$GetKey`()
