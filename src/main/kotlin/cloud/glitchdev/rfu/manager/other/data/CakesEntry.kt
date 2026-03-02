package cloud.glitchdev.rfu.manager.other.data

import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Instant

data class CakesEntry(
    val cakes : MutableList<Cake> = mutableListOf()
) : Entry {
    private fun getCake(name : String) : Cake? {
        return cakes.find { it.name == name }
    }

    fun getOutdatedCakes() : List<Cake> {
        return cakes.filter { it.isOutdated() }
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