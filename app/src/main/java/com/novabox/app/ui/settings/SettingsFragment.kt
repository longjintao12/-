package com.novabox.app.ui.settings

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.novabox.app.R
import com.novabox.app.databinding.FragmentSettingsBinding
import com.novabox.app.ui.cookie.CookieActivity
import com.novabox.app.ui.source.SourceManagerActivity

class SettingsFragment : Fragment() {

    private lateinit var b: FragmentSettingsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, state: Bundle?
    ): View {
        b = FragmentSettingsBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val items = listOf(
            SettingItem("▶", "源管理", "添加/编辑/测试影视数据源") {
                startActivity(Intent(requireContext(), SourceManagerActivity::class.java))
            },
            SettingItem("▶", "网盘Cookie", "夸克/UC/阿里等网盘Cookie") {
                startActivity(Intent(requireContext(), CookieActivity::class.java))
            },
            SettingItem("▶", "播放设置", "UA/Referer/自动连播/轮播等") {
                startActivity(Intent(requireContext(), SettingsActivity::class.java))
            },
            SettingItem("▶", "关于", "NovaBox 星盒 v1.0.0") {
                android.app.AlertDialog.Builder(requireContext())
                    .setTitle("关于 NovaBox")
                    .setMessage("版本 1.0.0\n\nMIT License\n完全自研影视聚合播放器")
                    .setPositiveButton("确定", null)
                    .show()
            }
        )

        b.recycler.layoutManager = LinearLayoutManager(requireContext())
        b.recycler.adapter = SettingsAdapter(items)
    }

    data class SettingItem(
        val icon: String,
        val title: String,
        val subtitle: String,
        val action: () -> Unit
    )

    class SettingsAdapter(
        private val items: List<SettingItem>
    ) : RecyclerView.Adapter<SettingsAdapter.VH>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_setting, parent, false)
            return VH(v)
        }

        override fun onBindViewHolder(h: VH, pos: Int) {
            val item = items[pos]
            h.icon.text = item.icon
            h.title.text = item.title
            h.subtitle.text = item.subtitle
            h.subtitle.visibility = if (item.subtitle.isNotBlank()) View.VISIBLE else View.GONE
            h.itemView.setOnClickListener { item.action() }
        }

        override fun getItemCount() = items.size

        class VH(v: View) : RecyclerView.ViewHolder(v) {
            val icon: TextView = v.findViewById(R.id.icon)
            val title: TextView = v.findViewById(R.id.title)
            val subtitle: TextView = v.findViewById(R.id.subtitle)
        }
    }
}
