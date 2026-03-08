package cloud.glitchdev.rfu.data.other

import cloud.glitchdev.rfu.data.other.data.Entry

data class OtherData (
    var savedStuff : MutableMap<String, Entry> = mutableMapOf()
)