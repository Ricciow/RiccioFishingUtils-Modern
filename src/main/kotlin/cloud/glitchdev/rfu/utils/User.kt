package cloud.glitchdev.rfu.utils

import cloud.glitchdev.rfu.RiccioFishingUtils.mc
import cloud.glitchdev.rfu.data.other.OtherManager
import cloud.glitchdev.rfu.data.other.data.StringEntry
import cloud.glitchdev.rfu.events.AutoRegister
import cloud.glitchdev.rfu.events.RegisteredEvent
import cloud.glitchdev.rfu.events.managers.ChatEvents

@AutoRegister
object User : RegisteredEvent {
    var profileId: String? = (OtherManager.getField("profile_id") as? StringEntry)?.value
        private set

    override fun register() {
        ChatEvents.registerAnyChatEvent(Regex("""Profile ID:\s*([a-fA-F0-9\-]+)""")) { _, matches ->
            val id = matches?.groupValues?.getOrNull(1)?.trim() ?: return@registerAnyChatEvent
            if (profileId != id) {
                profileId = id
                OtherManager.setField("profile_id", StringEntry(id))
                OtherManager.file.save()
                RFULogger.dev("Profile ID updated: $profileId")
            }
        }
    }

    fun getUsername() : String {
        return mc.user.name
    }

    fun isUser(username : String) : Boolean {
        return getUsername() == username
    }
}