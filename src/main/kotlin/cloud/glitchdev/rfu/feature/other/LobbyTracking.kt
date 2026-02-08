package cloud.glitchdev.rfu.feature.other

import cloud.glitchdev.rfu.config.categories.OtherSettings
import cloud.glitchdev.rfu.constants.text.TextColor
import cloud.glitchdev.rfu.constants.text.TextStyle
import cloud.glitchdev.rfu.events.managers.HypixelModApiEvents.registerLocationEvent
import cloud.glitchdev.rfu.feature.Feature
import cloud.glitchdev.rfu.feature.RFUFeature
import cloud.glitchdev.rfu.utils.Chat
import cloud.glitchdev.rfu.utils.TextUtils
import cloud.glitchdev.rfu.utils.World
import cloud.glitchdev.rfu.utils.dsl.toReadableString
import kotlin.time.Clock
import kotlin.time.Instant

@RFUFeature
object LobbyTracking : Feature {
    val visitedLobbies : HashMap<String, Instant> = hashMapOf()

    override fun onInitialize() {
        registerLocationEvent {
            if(OtherSettings.lobbyTracking) {
                val lobby = World.lobby
                val now = Clock.System.now()

                if(lobby != null) {
                    val lobbyResult = visitedLobbies[lobby]
                    if (lobbyResult != null) {
                       Chat.sendMessage(TextUtils.rfuLiteral("You've been on this lobby before! ${TextColor.GRAY}${now.minus(lobbyResult).toReadableString()} ago.",
                           TextStyle(TextColor.YELLOW)))
                    }

                    visitedLobbies[lobby] = now
                }
            }
        }
    }
}