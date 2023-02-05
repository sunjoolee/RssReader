package learningconcurrencyinkotlin.rssreader

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import kotlinx.coroutines.*
import org.w3c.dom.Element
import org.w3c.dom.Node
import java.nio.file.NotDirectoryException
import javax.xml.parsers.DocumentBuilderFactory

class MainActivity : AppCompatActivity() {
    private val dispatcher =newFixedThreadPoolContext(2, "IO")
    private val factory = DocumentBuilderFactory.newInstance()

    private val feeds = listOf(
        "https://www.npr.org/rss/rss.php?id=1001",
        "http://rss.cnn.com/rss/cnn_topstories.rss"
        // "http://feeds.foxnews.com/foxnews/politics?format=xml"
    )


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        asyncLoadNews()
    }

    private fun asyncLoadNews() =
        GlobalScope.launch{
            val requests = mutableListOf<Deferred<List<String>>>()

            //데이터 가져오기
            feeds.mapTo(requests){
                asyncFetchHeadLines(it, dispatcher)
            }
            requests.forEach {
                it.await()
            }

            //데이터 구성하기
            val headlines = requests.flatMap {
                it.getCompleted()
            }

            //UI에 표시하기
            val newsCountTextView = findViewById<TextView>(R.id.newsCountTextView)
            GlobalScope.launch(Dispatchers.Main) {
                newsCountTextView.text = "Found ${headlines.size} News in ${requests.size} feeds"
            }
    }

    private fun asyncFetchHeadLines(feed: String, dispatcher: CoroutineDispatcher) =
        GlobalScope.async(dispatcher){
            val builder = factory.newDocumentBuilder()
            Log.d("asyncFetchHeadLines", feed)
            val xml = builder.parse(feed)
            val news = xml.getElementsByTagName("channel").item(0)

            (0 until news.childNodes.length)
                .map { news.childNodes.item(it) }
                .filter { Node.ELEMENT_NODE == it.nodeType }
                .map{it as Element}
                .filter { "item" == it.tagName }
                .map { it.getElementsByTagName("title").item(0).textContent }
        }
}