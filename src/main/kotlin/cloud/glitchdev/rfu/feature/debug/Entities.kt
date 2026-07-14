package cloud.glitchdev.rfu.feature.debug

import cloud.glitchdev.rfu.utils.command.AbstractCommand

object Entities : AbstractCommand("entities") {
    override val description: String = "Commands for entity debugging (sb, bobbers, normal)"

    init {
        append(Sb)
        append(Bobbers)
        append(Normal)
    }
}
