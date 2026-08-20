package com.novabox.app.ui.favorite

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.google.android.material.tabs.TabLayout
import com.novabox.app.R
import com.novabox.app.data.db.AppDbHelper
import com.novabox.app.databinding.FragmentFavoriteBinding
import com.novabox.app.ui.detail.DetailActivity
import com.novabox.app.ui.player.PlayerActivity

class FavoriteFragment : Fragment() {

    private lateinit var b: FragmentFavoriteBinding
    private val db get() = AppDbHelper.get(requireContext())
    private val adapter = HistoryAdapter(
        onClick = { item ->
            if (b.tab.selectedTabPosition == 0) {
                // 收藏 -> 详情
                startActivity(Intent(requireContext(), DetailActivity::class.java).apply {
                    putExtra("sourceId", item["source_id"] as? String ?: "")
                    putExtra("vodId", item["vod_id"] as? String ?: "")
                    putExtra("name", item["name"] as? String ?: "")
                    putExtra("pic", item["pic"] as? String ?: "")
                })
            } else {
                // 历史 -> 播放
                startActivity(Intent(requireContext(), PlayerActivity::class.java).apply {
                    putExtra("sourceId", item["source_id"] as? String ?: "")
                    putExtra("vodId", item["vod_id"] as? String ?: "")
                    putExtra("name", item["name"] as? String ?: "")
                    putExtra("pic", item["pic"] as? String ?: "")
                    putExtra("playUrl", "")
                    putExtra("playIndex", item["play_index"] as? Int ?: 0)
                })
            }
        },
        onDelete = { item ->
            if (b.tab.selectedTabPosition == 0) {
                db.removeFavorite(
                    item["vod_id"] as? String ?: "",
                    item["source_id"] as? String ?: ""
                )
            } else {
                db.removeHistory(
                    item["vod_id"] as? String ?: "",
                    item["source_id"] as? String ?: ""
                )
            }
            loadData()
        }
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, state: Bundle?
    ): View {
        b = FragmentFavoriteBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        b.recycler.layoutManager = LinearLayoutManager(requireContext())
        b.recycler.adapter = adapter

        b.tab.addTab(b.tab.newTab().setText("收藏"))
        b.tab.addTab(b.tab.newTab().setText("历史"))
        b.tab.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) { loadData() }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        loadData()
    }

    private fun loadData() {
        val items = if (b.tab.selectedTabPosition == 0) {
            db.getFavorites()
        } else {
            db.getHistory()
        }
        adapter.submitList(items)
        b.empty.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
    }

    override fun onResume() {
        super.onResume()
        loadData()
    }
}

class HistoryAdapter(
    private val onClick: (Map<String, Any?>) -> Unit,
    private val onDelete: (Map<String, Any?>) -> Unit
) : RecyclerView.Adapter<HistoryAdapter.VH>() {

    private var items = listOf<Map<String, Any?>>()

    fun submitList(list: List<Map<String, Any?>>) {
        items = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_history, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(h: VH, pos: Int) {
        val item = items[pos]
        h.name.text = item["name"] as? String ?: ""
        val info = buildString {
            val pf = item["play_from"] as? String
            if (!pf.isNullOrBlank()) append(pf).append(" ")
            val pi = item["play_index"] as? Int
            if (pi != null) append("第${pi + 1}集")
        }
        h.info.text = info
        val pic = item["pic"] as? String
        if (!pic.isNullOrBlank()) h.image.load(pic)

        h.itemView.setOnClickListener { onClick(item) }
        h.delete.setOnClickListener { onDelete(item) }
    }

    override fun getItemCount() = items.size

    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val image: ImageView = v.findViewById(R.id.image)
        val name: TextView = v.findViewById(R.id.name)
        val info: TextView = v.findViewById(R.id.info)
        val delete: ImageView = v.findViewById(R.id.btn_delete)
    }
}
