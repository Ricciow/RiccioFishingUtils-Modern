package cloud.glitchdev.rfu.utils

import cloud.glitchdev.rfu.RiccioFishingUtils.minecraft

object User {
    fun getUsername() : String {
        return minecraft.session.username
    }

    fun isUser(username : String) : Boolean {
        return getUsername() == username
    }
}