package learningconcurrencyinkotlin.rssreader.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import learningconcurrencyinkotlin.rssreader.R
import learningconcurrencyinkotlin.rssreader.model.Article

interface ArticleLoader{
    suspend fun loadMore()
}
class ArticleAdapter() : RecyclerView.Adapter<ArticleAdapter.ArticleViewHolder>(){

    private val articles: MutableList<Article> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArticleViewHolder {
        val layout = LayoutInflater.from(parent.context)
            .inflate(R.layout.article_item, parent, false) as LinearLayout

        val feed = layout.findViewById<TextView>(R.id.feed)
        val title = layout.findViewById<TextView>(R.id.title)
        val summary = layout.findViewById<TextView>(R.id.summary)

        return ArticleViewHolder(layout, feed, title, summary)
    }

    override fun getItemCount() = when(articles){
            null -> 0
            else -> articles.size
        }



    override fun onBindViewHolder(holder: ArticleViewHolder, position: Int) {
        val article = articles[position]

        holder.feed.text = article.feed
        holder.title.text = article.title
        holder.summary.text = article.summary
    }

    fun add(moreArticles : List<Article>){
        this.articles.addAll(moreArticles)
        notifyDataSetChanged()
    }

    fun add(moreArticle: Article){
        this.articles.add(moreArticle)
        notifyDataSetChanged()
    }

    fun clear(){
        this.articles.clear()
        notifyDataSetChanged()
    }
    class ArticleViewHolder(
        val layout : LinearLayout,
        val feed : TextView,
        val title: TextView,
        val summary: TextView
    ):RecyclerView.ViewHolder(layout){

    }

}