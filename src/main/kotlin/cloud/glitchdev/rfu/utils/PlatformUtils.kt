package cloud.glitchdev.rfu.utils

import java.net.URI
//? if >=26.3 {
import com.mojang.blaze3d.Blaze3D
//?} else {
/*import net.minecraft.util.Util
*///?}

object PlatformUtils {
    fun openUri(url: String) {
        //? if >=26.3 {
        Blaze3D.openUri(URI.create(url))
        //?} else {
        /*Util.getPlatform().openUri(url)
        *///?}
    }
}
