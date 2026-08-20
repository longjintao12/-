package com.novabox.app.ui.search

import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import coil.load
import com.novabox.app.data.model.VodSummary
import com.novabox.app.data.repo.SourceRepo
import com.novabox.app.data.repo.VodRepo
import com.novabox.app.databinding.ActivitySearchBinding
import com.novabox.app.ui.detail.DetailActivity
import kotlinx.coroutines.launch

class SearchActivity : AppCompatActivity() {

    private lateinit var b: ActivitySearchBinding

    private val adapter = object : androidx.recyclerview.widget.ListAdapter<VodSummary, SearchVH>(
        com.novabox.app.ui.common.VodAdapter.DIFF
    ) {
        override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): SearchVH {
            val v = android.view.LayoutInflater.from(parent.context)
                .inflate(com.novabox.app.R.layout.item_search_result, parent, false)
            return SearchVH(v)
        }

        override fun onBindViewHolder(h: SearchVH, pos: Int) {
            val item = getItem(pos)
            h.name.text = item.vodName
            h.remarks.text = item.vodRemarks
            h.source.text = item.sourceName
            if (item.vodPic.isNotBlank()) h.image.load(item.vodPic)
            h.itemView.setOnClickListener {
                startActivity(Intent(this@SearchActivity, DetailActivity::class.java).apply {
                    putExtra("sourceId", item.sourceId)
                    putExtra("vodId", item.vodId)
                    putExtra("name", item.vodName)
                    putExtra("pic", item.vodPic)
                })
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(b.root)

        b.toolbar.setNavigationOnClickListener { finish() }
        b.recycler.layoutManager = LinearLayoutManager(this)
        b.recycler.adapter = adapter

        b.input.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                doSearch(b.input.text.toString().trim())
                true
            } else false
        }
    }

    private fun doSearch(wd: String) {
        if (wd.isBlank()) return
        b.progress.visibility = android.view.View.VISIBLE
        b.empty.visibility = android.view.View.GONE

        lifecycleScope.launch {
            val all = mutableListOf<VodSummary>()
            val sources = SourceRepo.load().filter { it.enabled }
            for (s in sources) {
                try {
                    val list = VodRepo.search(s, wd)
                    all.addAll(list)
                } catch (_: Exception) {}
            }
            adapter.submitList(all.toList())
            b.progress.visibility = android.view.View.GONE
            if (all.isEmpty()) {
                b.empty.visibility = android.view.View.VISIBLE
            }
        }
    }
}

class SearchVH(v: android.view.View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(v) {
    val image: android.widget.ImageView = v.findViewById(com.novabox.app.R.id.image)
    val name: android.widget.TextView = v.findViewById(com.novabox.app.R.id.name)
    val remarks: android.widget.TextView = v.findViewById(com.novabox.app.R.id.remarks)
    val source: android.widget.TextView = v.findViewById(com.novabox.app.R.id.source)
}
