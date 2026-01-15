package cloud.glitchdev.rfu.utils.rendering

import net.minecraft.client.render.Camera
import net.minecraft.util.math.Vec3d

fun Camera.getPosition() : Vec3d {
    //? if >=1.21.8 {
    return this.cameraPos
    //?} else {
    /*return this.pos
    *///?}
}