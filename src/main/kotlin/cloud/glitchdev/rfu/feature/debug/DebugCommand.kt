package cloud.glitchdev.rfu.feature.debug

import cloud.glitchdev.rfu.utils.command.AbstractCommand
import cloud.glitchdev.rfu.utils.command.Command

@Command
object DebugCommand : AbstractCommand("rfudebug") {
    override val description: String = "Command used for debugging rfu"
    init {
        append(Chat)
        append(Title)
        append(Entities)
    }
}