package learningconcurrencyinkotlin.rssreader.search

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.channels.actor

object ResultsCounter {
    private val counterContext = newSingleThreadContext("counter")
    private var counter = 0

    private val counterActor = GlobalScope.actor<Void?>(counterContext){
        for (msg in channel){
            counter++
        }
    }

    suspend fun increment() = counterActor.send(null)
}