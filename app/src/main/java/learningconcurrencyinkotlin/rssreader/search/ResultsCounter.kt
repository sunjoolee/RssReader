package learningconcurrencyinkotlin.rssreader.search

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.channels.actor

object ResultsCounter {
    private val counterContext = newSingleThreadContext("counter")
    private var counter = 0

    private val notificationChannel = Channel<Int>(Channel.CONFLATED)

    //UI에 채널을 노출하는 함수
    fun getNotificationChannel() : ReceiveChannel<Int> = notificationChannel

    private val counterActor = GlobalScope.actor<Void?>(counterContext){
        for (msg in channel){
            counter++
            notificationChannel.send(counter)
        }
    }

    suspend fun increment() = counterActor.send(null)
}