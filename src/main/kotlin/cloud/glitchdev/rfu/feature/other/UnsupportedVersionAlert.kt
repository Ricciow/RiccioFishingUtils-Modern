package cloud.glitchdev.rfu.feature.other

import cloud.glitchdev.rfu.RiccioFishingUtils.RFU_VERSION
import cloud.glitchdev.rfu.constants.text.TextColor.LIGHT_RED
import cloud.glitchdev.rfu.constants.text.TextColor.YELLOW
import cloud.glitchdev.rfu.constants.text.TextEffects.BOLD
import cloud.glitchdev.rfu.events.AutoRegister
import cloud.glitchdev.rfu.events.RegisteredEvent
import cloud.glitchdev.rfu.events.managers.ConnectionEvents.registerJoinEvent
import cloud.glitchdev.rfu.utils.Chat
import cloud.glitchdev.rfu.utils.TextUtils
import net.fabricmc.loader.api.Version

@AutoRegister
object UnsupportedVersionAlert : RegisteredEvent {
    private const val minVersion = "26.1"
    private val cutoffVersion = Version.parse("26.1")
    private val mcVersion = Version.parse(RFU_VERSION.friendlyString.substringAfter("+"))

    override fun register() {
        registerJoinEvent(10000) { wasConnected ->
            if (!wasConnected && mcVersion < cutoffVersion) {
                Chat.sendMessage(TextUtils.rfuLiteral(
                    "${LIGHT_RED}${BOLD}This minecraft version is no longer supported! (< $cutoffVersion)\n" +
                        "${YELLOW}Hypixel is no longer supporting this version on skyblock soon\n" +
                        "${YELLOW}Update to $minVersion or newer to continue receiving updates for RFU!"
                ))
            }
        }
    }
}