package cloud.glitchdev.rfu.feature.fishing

import cloud.glitchdev.rfu.config.categories.GeneralFishing
import cloud.glitchdev.rfu.events.managers.ChatEvents.registerGameEvent
import cloud.glitchdev.rfu.feature.Feature
import cloud.glitchdev.rfu.feature.RFUFeature
import cloud.glitchdev.rfu.utils.Chat
import cloud.glitchdev.rfu.utils.Party
import cloud.glitchdev.rfu.utils.dsl.toExactRegex
import net.minecraft.text.Text

@RFUFeature
object DoubleHookMessages : Feature {
    var currentIndex = 0

    override fun onInitialize() {
        registerGameEvent ("Double Hook\\!|It\\'s a Double Hook\\! Woot woot\\!|It's a Double Hook\\!".toExactRegex()) { _, _, _ ->
            if(GeneralFishing.toggleDoubleHookMessages) {
                var message = GeneralFishing.doubleHookMessages[currentIndex]

                //Loop through indices
                currentIndex = (currentIndex + 1) % GeneralFishing.doubleHookMessages.size

                if(GeneralFishing.randomDoubleHookMessages) {
                    message = GeneralFishing.doubleHookMessages.random()
                }

                if(Party.inParty) {
                    Chat.sendServerCommand("p $message")
                } else {
                    Chat.sendMessage(Text.literal(message))
                }
            }
        }
    }
}