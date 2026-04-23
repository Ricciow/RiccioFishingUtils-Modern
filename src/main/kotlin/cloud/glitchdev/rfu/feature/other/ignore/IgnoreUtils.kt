package cloud.glitchdev.rfu.feature.other.ignore

import cloud.glitchdev.rfu.constants.text.TextColor
import cloud.glitchdev.rfu.data.other.OtherManager
import cloud.glitchdev.rfu.data.other.data.StringSetEntry
import cloud.glitchdev.rfu.utils.TextUtils
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource

object IgnoreUtils {
    fun getIgnoredEntry(): StringSetEntry {
        return OtherManager.getField("ignored_users") { StringSetEntry() } as StringSetEntry
    }

    fun saveIgnoredEntry(entry: StringSetEntry) {
        OtherManager.setField("ignored_users", entry)
        OtherManager.file.save()
    }

    fun showHelp(source: FabricClientCommandSource) {
        source.sendFeedback(TextUtils.rfuLiteral("--- Hotspot Ignore Help ---", TextColor.YELLOW))
        source.sendFeedback(TextUtils.rfuLiteral("${TextColor.GOLD}/rfuignore add <username>${TextColor.GRAY} - Add a user to the ignore list"))
        source.sendFeedback(TextUtils.rfuLiteral("${TextColor.GOLD}/rfuignore remove <username>${TextColor.GRAY} - Remove a user from the ignore list"))
        source.sendFeedback(TextUtils.rfuLiteral("${TextColor.GOLD}/rfuignore removeall${TextColor.GRAY} - Clear the entire ignore list"))
        source.sendFeedback(TextUtils.rfuLiteral("${TextColor.GOLD}/rfuignore list {page}${TextColor.GRAY} - List ignored users"))
    }
}
