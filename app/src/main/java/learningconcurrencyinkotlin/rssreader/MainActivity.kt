package learningconcurrencyinkotlin.rssreader

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import kotlinx.coroutines.*
import learningconcurrencyinkotlin.rssreader.model.Article
import learningconcurrencyinkotlin.rssreader.model.Feed
import org.w3c.dom.Element
import org.w3c.dom.Node
import java.nio.file.NotDirectoryException
import javax.xml.parsers.DocumentBuilderFactory

class MainActivity : AppCompatActivity() {
    private val dispatcher = newFixedThreadPoolContext(2, "IO")
    private val factory = DocumentBuilderFactory.newInstance()

    private val feeds = listOf(
        Feed("npr", "https://www.npr.org/rss/rss.php?id=1001"),
        Feed("cnn", "http://rss.cnn.com/rss/cnn_topstories.rss"),
        // Feed("fox","http://feeds.foxnews.com/foxnews/politics?format=xml"),
        Feed("inv", "htt:myNewsFeed")
    )


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        asyncLoadNews()
    }

    private fun asyncLoadNews() =
        GlobalScope.launch {
            val requests = mutableListOf<Deferred<List<Article>>>()

            //데이터 가져오기
            feeds.mapTo(requests) {
                asyncFetchArticles(it, dispatcher)
            }
            requests.forEach {
                it.join()
            }

            //데이터 구성하기
            val articles = requests
                .filter { !it.isCancelled }
                .flatMap { it.getCompleted() }

            val failed = requests
                .filter { it.isCancelled }
                .size
            val obtained = requests.size - failed

            //UI에 표시하기
            GlobalScope.launch(Dispatchers.Main) {
                    //TODO: UI 갱신
            }
        }


    private fun asyncFetchArticles(feed: Feed, dispatcher: CoroutineDispatcher) =
        GlobalScope.async(dispatcher) {
            val builder = factory.newDocumentBuilder()

            val xml = builder.parse(feed.url)
            val news = xml.getElementsByTagName("channel").item(0)

            (0 until news.childNodes.length)
                .map { news.childNodes.item(it) }
                .filter { Node.ELEMENT_NODE == it.nodeType }
                .map { it as Element }
                .filter { "item" == it.tagName }
                .map {
                    val title = it.getElementsByTagName("title").item(0).textContent
                    val summary = it.getElementsByTagName("description").item(0).textContent
                    Article(feed.name, title, summary)
                }
        }
}