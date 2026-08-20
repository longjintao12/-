package com.novabox.app.ui.detail

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.google.android.material.tabs.TabLayout
import com.novabox.app.R
import com.novabox.app.data.db.AppDbHelper
import com.novabox.app.data.model.PlayUrl
import com.novabox.app.data.model.Source
import com.novabox.app.data.model.VodDetail
import com.novabox.app.data.repo.SourceRepo
import com.novabox.app.data.repo.VodRepo
import com.novabox.app.databinding.ActivityDetailBinding
import com.novabox.app.net.ApiParser
import com.novabox.app.ui.player.PlayerActivity
import kotlinx.coroutines.launch

class DetailActivity : AppCompatActivity() {

    private lateinit var b: ActivityDetailBinding
    private var detail: VodDetail? = null
    private var sourceId = ""
    private var vodId = ""
    private var vodName = ""
    private var vodPic = ""
    private var currentLine = 0
    private val episodeAdapter = EpisodeAdapter { ep -> playEpisode(currentLine, ep) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(b.root)

        b.toolbar.setNavigationOnClickListener { finish() }

        sourceId = intent.getStringExtra("sourceId") ?: ""
        vodId = intent.getStringExtra("vodId") ?: ""
        vodName = intent.getStringExtra("name") ?: ""
        vodPic = intent.getStringExtra("pic") ?: ""

        b.toolbar.title = vodName
        if (vodPic.isNotBlank()) b.poster.load(vodPic)

        b.episodes.layoutManager = GridLayoutManager(this, 5)
        b.episodes.adapter = episodeAdapter

        b.btnPlay.setOnClickListener {
            detail?.let { d ->
                if (d.vodPlayUrl.isNotEmpty() && d.vodPlayUrl[currentLine].isNotBlank()) {
                    val eps = ApiParser.parseEpisodes(d.vodPlayUrl[currentLine])
                    if (eps.isNotEmpty()) {
                        playEpisode(currentLine, eps.first())
                    }
                }
            }
        }

        b.tabPlayFrom.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.let {
                    currentLine = it.position
                    loadEpisodes()
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        loadDetail()
    }

    private fun loadDetail() {
        lifecycleScope.launch {
            try {
                val src = SourceRepo.load().find { it.id == sourceId }
                    ?: Source(sourceId, "", "", "", true, 0)
                val d = VodRepo.fetchDetail(src, vodId)
                if (d != null) {
                    detail = d
                    b.title.text = d.vodName
                    b.meta.text = buildString {
                        if (d.vodYear.isNotBlank()) append(d.vodYear).append(" · ")
                        if (d.vodArea.isNotBlank()) append(d.vodArea).append(" · ")
                        if (d.vodLang.isNotBlank()) append(d.vodLang)
                    }
                    b.remarks.text = d.vodRemarks
                    b.actor.text = if (d.vodActor.isNotBlank()) "演员: ${d.vodActor}" else ""
                    b.desc.text = d.vodContent
                    if (d.vodPic.isNotBlank()) b.poster.load(d.vodPic)

                    b.tabPlayFrom.removeAllTabs()
                    for (line in d.vodPlayFrom) {
                        b.tabPlayFrom.addTab(b.tabPlayFrom.newTab().setText(line))
                    }
                    loadEpisodes()
                }
            } catch (e: Exception) {
                Toast.makeText(this@DetailActivity, "加载失败: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadEpisodes() {
        detail?.let { d ->
            if (d.vodPlayUrl.size > currentLine) {
                val eps = ApiParser.parseEpisodes(d.vodPlayUrl[currentLine])
                episodeAdapter.setData(eps)
            }
        }
    }

    private fun playEpisode(line: Int, ep: PlayUrl) {
        val db = AppDbHelper.get(this)
        db.addHistory(vodId, sourceId, vodName, vodPic,
            detail?.vodPlayFrom?.let { if (it.size > line) it[line] else "" } ?: "", line, 0)

        startActivity(Intent(this, PlayerActivity::class.java).apply {
            putExtra("sourceId", sourceId)
            putExtra("vodId", vodId)
            putExtra("name", vodName)
            putExtra("pic", vodPic)
            putExtra("playFrom", detail?.vodPlayFrom?.let { if (it.size > line) it[line] else "" } ?: "")
            putExtra("playIndex", episodeAdapter.getData().indexOf(ep))
            putExtra("playUrl", ep.url)
            putExtra("epName", ep.name)
        })
    }

    class EpisodeAdapter(
        private val onClick: (PlayUrl) -> Unit
    ) : RecyclerView.Adapter<EpisodeAdapter.VH>() {

        private var data = listOf<PlayUrl>()

        fun setData(list: List<PlayUrl>) {
            data = list
            notifyDataSetChanged()
        }

        fun getData() = data

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_episode, parent, false)
            return VH(v)
        }

        override fun onBindViewHolder(h: VH, pos: Int) {
            val ep = data[pos]
            h.name.text = ep.name
            h.name.setOnClickListener { onClick(ep) }
        }

        override fun getItemCount() = data.size

        class VH(v: View) : RecyclerView.ViewHolder(v) {
            val name: TextView = v.findViewById(R.id.name)
        }
    }
}
