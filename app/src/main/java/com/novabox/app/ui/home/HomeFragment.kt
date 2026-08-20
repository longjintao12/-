package com.novabox.app.ui.home

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.novabox.app.R
import com.novabox.app.data.model.Category
import com.novabox.app.data.model.HomeItem
import com.novabox.app.data.model.Source
import com.novabox.app.data.model.VodSummary
import com.novabox.app.data.repo.SourceRepo
import com.novabox.app.data.repo.VodRepo
import com.novabox.app.databinding.FragmentHomeBinding
import com.novabox.app.ui.category.CategoryActivity
import com.novabox.app.ui.detail.DetailActivity
import com.novabox.app.ui.search.SearchActivity
import com.novabox.app.util.Prefs
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private lateinit var b: FragmentHomeBinding
    private val adapter = HomeAdapter(
        onVodClick = { v -> openDetail(v) },
        onCategoryClick = { c -> openCategory(c) },
        onMoreClick = { c -> openCategory(c) }
    )
    private val handler = Handler(Looper.getMainLooper())
    private var bannerRunnable: Runnable? = null
    private var currentSource: Source? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, state: Bundle?
    ): View {
        b = FragmentHomeBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        b.recycler.layoutManager = LinearLayoutManager(requireContext())
        b.recycler.adapter = adapter
        b.swipe.setOnRefreshListener { loadData() }
        loadData()
    }

    private fun loadData() {
        val sources = SourceRepo.load().filter { it.enabled }
        if (sources.isEmpty()) {
            b.swipe.isRefreshing = false
            toast("暂无数据源，请在「我的」中添加")
            return
        }
        val source = if (Prefs.lastSourceId.isNotBlank())
            sources.find { it.id == Prefs.lastSourceId } ?: sources.first()
        else sources.first()
        currentSource = source

        lifecycleScope.launch {
            try {
                val (cats, list) = VodRepo.fetchHome(source)
                val items = mutableListOf<HomeItem>()

                if (Prefs.bannerEnable && list.isNotEmpty()) {
                    items.add(HomeItem.Banner(list.take(10)))
                }
                if (cats.isNotEmpty()) {
                    items.add(HomeItem.Cats(cats))
                }
                if (list.isNotEmpty()) {
                    items.add(HomeItem.Row("推荐", list.take(20)))
                }

                // 取前几个分类的内容
                if (cats.isNotEmpty()) {
                    val topCats = cats.take(6)
                    for (cat in topCats) {
                        try {
                            val catList = VodRepo.fetchCategoryList(source, cat.id, 1)
                            if (catList.isNotEmpty()) {
                                items.add(HomeItem.Row(cat.name, catList))
                            }
                        } catch (_: Exception) {}
                    }
                }

                adapter.submitList(items)
                startBanner()
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

    private fun openCategory(c: Category) {
        val src = currentSource ?: return
        startActivity(Intent(requireContext(), CategoryActivity::class.java).apply {
            putExtra("sourceId", src.id)
            putExtra("sourceName", src.name)
            putExtra("catId", c.id)
            putExtra("catName", c.name)
        })
    }

    private fun startBanner() {
        stopBanner()
        val items = adapter.currentList
        val bannerIdx = items.indexOfFirst { it is HomeItem.Banner }
        if (bannerIdx < 0) return
        val vp = b.recycler.findViewHolderForAdapterPosition(bannerIdx)
            ?.itemView?.findViewById<ViewPager2>(R.id.banner_pager) ?: return
        if (vp.adapter?.itemCount ?: 0 <= 1) return

        bannerRunnable = object : Runnable {
            override fun run() {
                val next = (vp.currentItem + 1) % (vp.adapter?.itemCount ?: 1)
                vp.setCurrentItem(next, true)
                handler.postDelayed(this, 4000)
            }
        }
        handler.postDelayed(bannerRunnable!!, 4000)
    }

    private fun stopBanner() {
        bannerRunnable?.let { handler.removeCallbacks(it) }
        bannerRunnable = null
    }

    override fun onDestroyView() {
        stopBanner()
        super.onDestroyView()
    }

    private fun toast(msg: String) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
    }
}
