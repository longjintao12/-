package com.novabox.app.ui.source

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.novabox.app.R
import com.novabox.app.data.model.Source
import com.novabox.app.data.repo.SourceRepo
import com.novabox.app.databinding.ActivitySourceManagerBinding
import com.novabox.app.net.ApiClient
import kotlinx.coroutines.launch
import java.util.UUID

class SourceManagerActivity : AppCompatActivity() {

    private lateinit var b: ActivitySourceManagerBinding
    private lateinit var adapter: SourceAdapter
    private var sources: MutableList<Source> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivitySourceManagerBinding.inflate(layoutInflater)
        setContentView(b.root)

        b.toolbar.setNavigationOnClickListener { finish() }

        sources = SourceRepo.load()
        adapter = SourceAdapter(sources,
            onToggle = { s, enabled ->
                s.enabled = enabled
                SourceRepo.save(sources)
            },
            onDelete = { s ->
                sources.remove(s)
                adapter.notifyDataSetChanged()
                SourceRepo.save(sources)
                updateEmpty()
            },
            onTest = { s -> testSource(s) }
        )

        b.recycler.layoutManager = LinearLayoutManager(this)
        b.recycler.adapter = adapter

        b.fab.setOnClickListener { showAddDialog() }
        updateEmpty()
    }

    private fun showAddDialog() {
        val edit = android.widget.EditText(this).apply { hint = "名称" }
        val editApi = android.widget.EditText(this).apply { hint = "API 地址" }
        val layout = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            addView(edit)
            addView(editApi)
        }
        AlertDialog.Builder(this)
            .setTitle("添加数据源")
            .setView(layout)
            .setPositiveButton("确认") { _, _ ->
                val name = edit.text.toString().trim()
                val api = editApi.text.toString().trim()
                if (name.isNotBlank() && api.isNotBlank()) {
                    val s = Source(UUID.randomUUID().toString(), name, api, "", true, sources.size)
                    sources.add(s)
                    adapter.notifyItemInserted(sources.size - 1)
                    SourceRepo.save(sources)
                    updateEmpty()
                }
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun testSource(s: Source) {
        lifecycleScope.launch {
            try {
                val url = s.api.trim().trimEnd('/') + "?ac=list"
                ApiClient.getString(url)
                Toast.makeText(this@SourceManagerActivity, "${s.name} 连接成功", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this@SourceManagerActivity, "${s.name} 失败: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateEmpty() {
        b.empty.visibility = if (sources.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
    }
}

class SourceAdapter(
    private val data: List<Source>,
    private val onToggle: (Source, Boolean) -> Unit,
    private val onDelete: (Source) -> Unit,
    private val onTest: (Source) -> Unit
) : RecyclerView.Adapter<SourceAdapter.VH>() {

    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): VH {
        val v = android.view.LayoutInflater.from(parent.context)
            .inflate(R.layout.item_source, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(h: VH, pos: Int) {
        val s = data[pos]
        h.name.text = s.name
        h.api.text = s.api
        h.enabled.isChecked = s.enabled
        h.enabled.setOnCheckedChangeListener { _, v -> onToggle(s, v) }
        h.delete.setOnClickListener { onDelete(s) }
        h.test.setOnClickListener { onTest(s) }
    }

    override fun getItemCount() = data.size

    class VH(v: android.view.View) : RecyclerView.ViewHolder(v) {
        val name: android.widget.TextView = v.findViewById(R.id.name)
        val api: android.widget.TextView = v.findViewById(R.id.api)
        val enabled: androidx.appcompat.widget.SwitchCompat = v.findViewById(R.id.switch_enabled)
        val delete: android.widget.ImageButton = v.findViewById(R.id.btn_delete)
        val test: android.widget.Button = v.findViewById(R.id.btn_test)
    }
}
