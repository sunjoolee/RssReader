package learningconcurrencyinkotlin.rssreader.search

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import learningconcurrencyinkotlin.rssreader.model.Article

class Searcher {
    fun search(query: String): ReceiveChannel<Article> {
        return Channel(150)
    }
}