package cloud.glitchdev.rfu.data.other.data

data class StringSetEntry(
    val strings: MutableSet<String> = mutableSetOf()
) : Entry {
    fun add(user: String) = strings.add(user.lowercase())
    fun remove(user: String) = strings.remove(user.lowercase())
    fun contains(user: String) = strings.contains(user.lowercase())
    fun getAll() = strings.toList().sorted()
    fun clear() = strings.clear()
}
