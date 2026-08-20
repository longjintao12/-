package com.novabox.app.ui.category

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.novabox.app.data.model.Source
import com.novabox.app.data.repo.VodRepo
import com.novabox.app.databinding.ActivityCategoryBinding
import com.novabox.app.ui.common.VodAdapter
import com.novabox.app.ui.detail.DetailActivity
import kotlinx.coroutines.launch

class CategoryActivity : AppCompatActivity() {

    private lateinit var b: ActivityCategoryBinding
    private val adapter = VodAdapter { v ->
        startActivity(Intent(this, DetailActivity::class.java).apply {
            putExtra("sourceId", v.sourceId)
            putExtra("vodId", v.vodId)
            putExtra("name", v.vodName)
            putExtra("pic", v.vodPic)
        })
    }

    private val source = Source("", "", "")
    private var catId = ""
    private var page = 1
    private var isLoading = false
    private var hasMore = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityCategoryBinding.inflate(layoutInflater)
        setContentView(b.root)

        b.toolbar.setNavigationOnClickListener { finish() }

        val sourceId = intent.getStringExtra("sourceId") ?: ""
        val sourceName = intent.getStringExtra("sourceName") ?: ""
        catId = intent.getStringExtra("catId") ?: ""
        val catName = intent.getStringExtra("catName") ?: ""
        b.toolbar.title = catName

        (source as java.util.HashMap).putAll(mapOf("id" to sourceId, "name" to sourceName, "api" to "", "enabled" to true, "order" to 0))
        // hack: 直接构造 Source 对象
        val s = com.novabox.app.data.model.Source(
            id = sourceId, name = sourceName, api = "", key = "", enabled = true, order = 0
        )

        b.recycler.layoutManager = GridLayoutManager(this, 3)
        b.recycler.adapter = adapter
        b.swipe.setOnRefreshListener {
            page = 1
            hasMore = true
            adapter.submitList(emptyList())
            loadData(s)
        }

        b.recycler.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (!isLoading && hasMore) {
                    val lm = recyclerView.layoutManager as? GridLayoutManager ?: return
                    if (lm.findLastVisibleItemPosition() >= adapter.itemCount - 3) {
                        loadData(s)
                    }
                }
            }
        })

        loadData(s)
    }

    private fun loadData(s: com.novabox.app.data.model.Source) {
        if (isLoading || !hasMore) return
        isLoading = true
        lifecycleScope.launch {
            try {
                val list = VodRepo.fetchCategoryList(s, catId, page)
                if (list.isEmpty()) {
                    hasMore = false
                } else {
                    val current = adapter.currentList.toMutableList()
                    current.addAll(list)
                    adapter.submitList(current)
                    page++
                }
            } catch (e: Exception) {
                Toast.makeText(this@CategoryActivity, "加载失败: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                isLoading = false
                b.swipe.isRefreshing = false
            }
        }
    }
}
