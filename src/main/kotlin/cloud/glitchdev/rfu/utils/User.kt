package cloud.glitchdev.rfu.utils

import cloud.glitchdev.rfu.RicciosFinestUtilities.Companion.minecraft

object User {
    fun getUsername() : String {
        return minecraft.session.username
    }
}