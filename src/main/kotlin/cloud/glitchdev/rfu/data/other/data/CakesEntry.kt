package cloud.glitchdev.rfu.data.other.data

import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

data class CakesEntry(
    val cakes : MutableList<Cake> = mutableListOf()
) : Entry {
    private fun getCake(name : String) : Cake? {
        return cakes.find { it.name == name }
    }

    fun clearCakeList() {
        cakes.clear()
    }

    fun getOutdatedCakes() : List<Cake> {
        return cakes.filter { it.isOutdated() }
    }

    fun expireAllCakes(): Boolean {
        val activeCakes = cakes.filterNot { it.isOutdated() }
        if (activeCakes.isEmpty()) return false

        val expiredDate = Clock.System.now() - 2.days - 1.seconds
        activeCakes.forEach { it.date = expiredDate }
        return true
    }

    fun refreshOutdatedCakes(remaining: Duration): Boolean {
        if (remaining <= Duration.ZERO) return false
        val outdatedCakes = getOutdatedCakes()
        if (outdatedCakes.isEmpty()) return false

        val refreshedDate = Clock.System.now() - 2.days + remaining
        outdatedCakes.forEach { it.date = refreshedDate }
        return true
    }

    fun eatCake(name: String) {
        val time = Clock.System.now()
        val cake = getCake(name)

        if(cake != null) {
            cake.date = time
        } else {
            cakes.add(Cake(name, time))
        }
    }

    data class Cake(
        val name : String,
        var date : Instant
    ) {
        fun isOutdated() : Boolean {
            return Clock.System.now() - date > 2.days
        }
    }
}
