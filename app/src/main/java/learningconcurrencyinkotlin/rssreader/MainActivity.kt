package learningconcurrencyinkotlin.rssreader

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Contacts.Intents.UI
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.*
import learningconcurrencyinkotlin.rssreader.adapter.ArticleAdapter
import learningconcurrencyinkotlin.rssreader.adapter.ArticleLoader
import learningconcurrencyinkotlin.rssreader.model.Article
import learningconcurrencyinkotlin.rssreader.model.Feed
import learningconcurrencyinkotlin.rssreader.producer.ArticleProducer
import org.w3c.dom.Element
import org.w3c.dom.Node
import java.nio.file.NotDirectoryException
import javax.xml.parsers.DocumentBuilderFactory

class MainActivity : AppCompatActivity(), ArticleLoader{

    private lateinit var articleRcyclerView: RecyclerView
    private lateinit var articleAdapter: ArticleAdapter
    private lateinit var articleLayoutManager: RecyclerView.LayoutManager


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        articleRcyclerView = findViewById(R.id.articleRecyclerView)
        //어댑터의 articleLoader로 this 전달
        articleAdapter = ArticleAdapter()
        articleLayoutManager = LinearLayoutManager(this)

        articleRcyclerView.apply{
            adapter = articleAdapter
            layoutManager = articleLayoutManager
        }

        GlobalScope.launch {
            loadMore()
        }
    }

    override suspend fun loadMore() {
        val producer = ArticleProducer.producer

        //프로듀서 열려있는지 검사 후 기사 더 요청
        if(!producer.isClosedForReceive){
            val articles = producer.receive()

            GlobalScope.launch(Dispatchers.Main){
                findViewById<ProgressBar>(R.id.progressBar).visibility = View.GONE
                articleAdapter.add(articles)
            }
        }
    }
}