package com.novabox.app.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.novabox.app.data.model.Category
import com.novabox.app.data.model.Source
import com.novabox.app.data.model.VodSummary
import com.novabox.app.data.repo.SourceRepo
import com.novabox.app.data.repo.VodRepo
import com.novabox.app.databinding.FragmentHomeBinding
import com.novabox.app.ui.category.CategoryActivity
import com.novabox.app.ui.common.VodAdapter
import com.novabox.app.ui.detail.DetailActivity
import com.novabox.app.util.Prefs
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private lateinit var b: FragmentHomeBinding
    private val adapter = VodAdapter { v -> openDetail(v) }
    private var currentSource: Source? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, state: Bundle?
    ): View {
        b = FragmentHomeBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        b.recycler.layoutManager = GridLayoutManager(requireContext(), 3)
        b.recycler.adapter = adapter
        b.swipe.setOnRefreshListener { loadData() }
        loadData()
    }

    private fun loadData() {
        val sources = SourceRepo.load().filter { it.enabled }
        if (sources.isEmpty()) {
            b.swipe.isRefreshing = false
            Toast.makeText(requireContext(), "暂无数据源，请在「我的」中添加", Toast.LENGTH_SHORT).show()
            return
        }
        val source = if (Prefs.lastSourceId.isNotBlank())
            sources.find { it.id == Prefs.lastSourceId } ?: sources.first()
        else sources.first()
        currentSource = source

        lifecycleScope.launch {
            try {
                val (_, list) = VodRepo.fetchHome(source)
                adapter.submitList(list.take(60))
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "加载失败: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                b.swipe.isRefreshing = false
            }
        }
    }

    private fun openDetail(v: VodSummary) {
        startActivity(Intent(requireContext(), DetailActivity::class.java).apply {
            putExtra("sourceId", v.sourceId)
            putExtra("vodId", v.vodId)
            putExtra("name", v.vodName)
            putExtra("pic", v.vodPic)
        })
    }
}
