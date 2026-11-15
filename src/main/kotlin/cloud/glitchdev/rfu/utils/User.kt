package cloud.glitchdev.rfu.utils

import cloud.glitchdev.rfu.RiccioFishingUtils.mc
import cloud.glitchdev.rfu.data.other.OtherManager
import cloud.glitchdev.rfu.data.other.data.StringEntry
import cloud.glitchdev.rfu.events.AutoRegister
import cloud.glitchdev.rfu.events.RegisteredEvent
import cloud.glitchdev.rfu.events.managers.ChatEvents
import cloud.glitchdev.rfu.model.network.ProfileSyncResponse
import cloud.glitchdev.rfu.model.network.WebSocketEvent
import cloud.glitchdev.rfu.model.network.WebSocketEventType
import cloud.glitchdev.rfu.party.PartyRequirementsManager
import cloud.glitchdev.rfu.utils.network.WebSocketClient
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@AutoRegister
object User : RegisteredEvent {
    var profileId: String? = (OtherManager.getField("profile_id") as? StringEntry)?.value
        private set

    internal fun sameProfileId(first: String?, second: String?): Boolean =
        first?.trim()?.replace("-", "").orEmpty().equals(second?.trim()?.replace("-", "").orEmpty(), ignoreCase = true)

    override fun register() {
        WebSocketClient.subscribe("/user/queue/profile") { message ->
            try {
                val type = object : TypeToken<WebSocketEvent<ProfileSyncResponse>>() {}.type
                val event = Gson().fromJson<WebSocketEvent<ProfileSyncResponse>>(message, type)
                val correction = event.data
                if (event.type == WebSocketEventType.SYNC && correction != null) {
                    mc.execute {
                        if (correction.canApplyTo(mc.user.profileId.toString(), profileId)) {
                            updateProfileId(correction.profileId!!)
                        }
                    }
                }
            } catch (error: Exception) {
                RFULogger.error("Error parsing profile sync: ", error)
            }
        }

        ChatEvents.registerAnyChatEvent(Regex("""Profile ID:\s*([a-fA-F0-9\-]+)""")) { _, matches ->
            val id = matches?.groupValues?.getOrNull(1)?.trim() ?: return@registerAnyChatEvent
            updateProfileId(id)
        }
    }

    private fun updateProfileId(id: String) {
        if (sameProfileId(profileId, id)) return
        profileId = id
        OtherManager.setField("profile_id", StringEntry(id))
        OtherManager.file.save()
        RFULogger.info("Profile ID updated: $profileId")
        if (!sameProfileId(PartyRequirementsManager.cachedProfileId, id)) {
            PartyRequirementsManager.clearPlayerRequisites()
        }
    }

    fun getUsername() : String {
        return mc.user.name
    }

    fun isUser(username : String) : Boolean {
        return getUsername() == username
    }
}