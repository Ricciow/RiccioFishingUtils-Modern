package cloud.glitchdev.rfu.utils.network
import cloud.glitchdev.rfu.RiccioFishingUtils.API_URL
import cloud.glitchdev.rfu.RiccioFishingUtils.RFU_VERSION
import cloud.glitchdev.rfu.constants.text.TextColor.*
import cloud.glitchdev.rfu.events.AutoRegister
import cloud.glitchdev.rfu.events.RegisteredEvent
import cloud.glitchdev.rfu.events.managers.ConnectionEvents.registerJoinEvent
import cloud.glitchdev.rfu.model.version.ModVersion
import cloud.glitchdev.rfu.utils.Chat
import cloud.glitchdev.rfu.utils.RFULogger
import cloud.glitchdev.rfu.utils.TextUtils
import com.google.gson.Gson
import net.fabricmc.loader.api.Version
import net.minecraft.network.chat.Component

@AutoRegister
object VersionHttp : RegisteredEvent{
    private val gson = Gson()
    private val currentVersion = Version.parse(RFU_VERSION.friendlyString.substringBefore("+"))
    var isOutdated = false
        private set

    override fun register() {
        registerJoinEvent { wasConnected ->
            if(!wasConnected) {
                verifyVersion()
            }
        }
    }

    fun verifyVersion() {
        getLatestVersion { modVersion ->
            if(modVersion != null) {
                val latest = modVersion.toVersion()
                if(latest > currentVersion) {
                    isOutdated = true

                    val text = TextUtils.rfuLiteral("Your mod is outdated!", LIGHT_RED)

                    text.append(
                        Component.literal("\n${YELLOW}Latest Version is $WHITE${latest.friendlyString}$YELLOW, you're on version $WHITE${currentVersion.friendlyString}")
                    )

                    Chat.sendMessage(text)
                }
            } else {
                Chat.sendMessage(TextUtils.rfuLiteral("Unable to verify latest version, Who knows if it is outdated...",
                    LIGHT_RED))
            }
        }
    }

    fun getLatestVersion(callback : (ModVersion?) -> Unit) {
        Network.getRequest("$API_URL/version") { response ->
            var result : ModVersion? = null

            if(response.isSuccessful()) {
                try {
                    result = gson.fromJson(response.body, ModVersion::class.java)
                } catch (e: Exception) {
                    RFULogger.error("Failed to decode latest version", e)
                }
            }

            callback(result)
        }
    }
}