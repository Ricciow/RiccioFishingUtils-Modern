package cloud.glitchdev.rfu.utils

import cloud.glitchdev.rfu.RiccioFishingUtils.mc

object User {
    fun getUsername() : String {
        return mc.user.name
    }

    fun isUser(username : String) : Boolean {
        return getUsername() == username
    }
}