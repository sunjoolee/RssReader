package learningconcurrencyinkotlin.rssreader.search

import android.util.Log
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.launch
import kotlinx.coroutines.newFixedThreadPoolContext
import learningconcurrencyinkotlin.rssreader.model.Article
import learningconcurrencyinkotlin.rssreader.model.Feed
import learningconcurrencyinkotlin.rssreader.producer.ArticleProducer
import org.w3c.dom.Element
import org.w3c.dom.Node
import javax.xml.parsers.DocumentBuilderFactory

class Searcher {
    private val dispatcher = newFixedThreadPoolContext(3, "IO")
    private val factory = DocumentBuilderFactory.newInstance()

    private val feeds = listOf(
        Feed("npr", "https://www.npr.org/rss/rss.php?id=1001"),
        Feed("cnn", "http://rss.cnn.com/rss/cnn_topstories.rss")
        // Feed("fox","http://feeds.foxnews.com/foxnews/politics?format=xml")
    )

    //쿼리를 받아서 ReceiveChannel<Article>을 반환하는 공개 함수
    fun search(query: String): ReceiveChannel<Article> {
        val channel = Channel<Article>(150)

        feeds.forEach { feed->
            GlobalScope.launch(dispatcher){
                search(feed, channel, query)
            }
        }
        return channel
    }

    //피드, 쿼리, 채널을 갖고 실제 검색을 하는 비공개 함수
    private suspend fun search(
        feed: Feed,
        channel: SendChannel<Article>,
        query: String){

            val builder = factory.newDocumentBuilder()
            val xml = builder.parse(feed.url)
            val news = xml.getElementsByTagName("channel").item(0)

            return (0 until news.childNodes.length)
                .map { news.childNodes.item(it) }
                .filter { Node.ELEMENT_NODE == it.nodeType }
                .map { it as Element }
                .filter { "item" == it.tagName }
                .forEach {
                    if(it == null){
                        Log.d("Searcher", "No search result found")
                        return@forEach
                    }

                    val title = it.getElementsByTagName("title").item(0)?.textContent
                    var summary = it.getElementsByTagName("description").item(0)?.textContent

                    if((title == null) || (summary == null)){
                        Log.d("Searcher", "No title/summary found")
                        return@forEach
                    }

                    //모든 컨텐츠를 매핑하는 대신 필터링 한 기사를 채널을 통해 전송
                    if(title.contains(query) || summary.contains(query)) {

                        if (!summary.startsWith("<div") && summary.contains("<div")) {
                            summary = summary.substring(0, summary.indexOf("<div"))
                        }

                        val article = Article(feed.name, title, summary)
                        channel.send(article)
                        //카운터를 갖는 싱글톤 증가
                        ResultsCounter.increment()
                    }
                }
    }
}