package cloud.glitchdev.rfu.data.party

import cloud.glitchdev.rfu.data.other.OtherManager
import cloud.glitchdev.rfu.data.other.data.PartyPresetsEntry

object PartyPresetsManager {
    private const val PRESETS_KEY = "party_finder_presets"

    fun getEntry(): PartyPresetsEntry {
        return OtherManager.getField(PRESETS_KEY) { PartyPresetsEntry() } as? PartyPresetsEntry
            ?: PartyPresetsEntry()
    }

    fun saveEntry(entry: PartyPresetsEntry) {
        OtherManager.setField(PRESETS_KEY, entry)
        OtherManager.file.save()
    }
}
