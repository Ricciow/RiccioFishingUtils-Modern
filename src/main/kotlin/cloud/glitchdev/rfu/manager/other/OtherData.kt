package cloud.glitchdev.rfu.manager.other

import cloud.glitchdev.rfu.manager.other.data.Entry

data class OtherData (
    var savedStuff : MutableMap<String, Entry> = mutableMapOf()
)