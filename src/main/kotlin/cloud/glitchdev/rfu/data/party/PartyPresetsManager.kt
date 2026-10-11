package cloud.glitchdev.rfu.data.party

import cloud.glitchdev.rfu.constants.text.Emoji
import cloud.glitchdev.rfu.data.other.OtherManager
import cloud.glitchdev.rfu.data.other.data.PartyPresetsEntry

object PartyPresetsManager {
    private const val PRESETS_KEY = "party_finder_presets"
    private const val CURRENT_VERSION = 1

    fun getEntry(): PartyPresetsEntry {
        val entry = (OtherManager.getField(PRESETS_KEY) { PartyPresetsEntry(version = CURRENT_VERSION) } as? PartyPresetsEntry)
            ?: PartyPresetsEntry(version = CURRENT_VERSION)
        if (migrate(entry)) saveEntry(entry)
        return entry
    }

    fun saveEntry(entry: PartyPresetsEntry) {
        OtherManager.setField(PRESETS_KEY, entry)
        OtherManager.file.save()
    }

    private fun migrate(entry: PartyPresetsEntry): Boolean {
        if (entry.version >= CURRENT_VERSION) return false
        for (preset in entry.presets.values + listOfNotNull(entry.lastPartyState)) {
            preset.name = Emoji.convertLegacyEmojis(preset.name)
            preset.title = Emoji.convertLegacyEmojis(preset.title)
            preset.description = Emoji.convertLegacyEmojis(preset.description)
        }
        entry.version = CURRENT_VERSION
        return true
    }
}
