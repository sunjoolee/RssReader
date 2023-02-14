package learningconcurrencyinkotlin.rssreader

import android.os.Bundle
import android.os.PersistableBundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import learningconcurrencyinkotlin.rssreader.adapter.ArticleAdapter
import learningconcurrencyinkotlin.rssreader.model.Article
import learningconcurrencyinkotlin.rssreader.search.ResultsCounter
import learningconcurrencyinkotlin.rssreader.search.Searcher

class SearchActivity : AppCompatActivity(){

    private val searcher  = Searcher()

    private lateinit var articleRcyclerView: RecyclerView
    private lateinit var articleAdapter: ArticleAdapter
    private lateinit var articleLayoutManager: RecyclerView.LayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        articleLayoutManager = LinearLayoutManager(this)
        articleAdapter = ArticleAdapter()

        articleRcyclerView = findViewById<RecyclerView>(R.id.articleRecyclerView).apply {
            layoutManager = articleLayoutManager
            adapter = articleAdapter
        }

        findViewById<Button>(R.id.searchButton).setOnClickListener{
            articleAdapter.clear()
            GlobalScope.launch{
                ResultsCounter.reset()
                search()
            }
        }

        GlobalScope.launch {
            updateCounter()
        }
    }

    private suspend fun search(){
        val query : String = findViewById<EditText>(R.id.searchEditText).text.toString()

        val channel = searcher.search(query)
        while (!channel.isClosedForReceive){
            val article = channel.receive()

            GlobalScope.launch(Dispatchers.Main){
                articleAdapter.add(article)
            }
        }
    }

    suspend fun updateCounter(){
        val notificationChannel = ResultsCounter.getNotificationChannel()
        val resultsTextView = findViewById<TextView>(R.id.resultsTextView)

        while (!notificationChannel.isClosedForReceive){
            val newAmount = notificationChannel.receive()

            withContext(Dispatchers.Main){
                resultsTextView.text = "$newAmount results found"
            }
        }
    }
}