package com.novabox.app.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import coil.load
import com.novabox.app.R
import com.novabox.app.data.model.Category
import com.novabox.app.data.model.HomeItem
import com.novabox.app.data.model.VodSummary

class HomeAdapter(
    private val onVodClick: (VodSummary) -> Unit,
    private val onCategoryClick: (Category) -> Unit,
    private val onMoreClick: (Category) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var items: List<HomeItem> = emptyList()

    fun submitList(list: List<HomeItem>) {
        items = list
        notifyDataSetChanged()
    }

    val currentList: List<HomeItem> get() = items

    override fun getItemViewType(position: Int): Int = when (items[position]) {
        is HomeItem.Banner -> 0
        is HomeItem.Cats -> 1
        is HomeItem.Row -> 2
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            0 -> {
                val v = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_banner, parent, false)
                BannerVH(v)
            }
            1 -> CatsVH(LayoutInflater.from(parent.context).inflate(R.layout.item_category, parent, false) as TextView)
            2 -> RowVH(LayoutInflater.from(parent.context).inflate(R.layout.item_vod_h, parent, false))
            else -> throw IllegalArgumentException()
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is HomeItem.Banner -> bindBanner(holder as BannerVH, item)
            is HomeItem.Cats -> bindCats(holder as CatsVH, item)
            is HomeItem.Row -> (holder as RowVH).bind(item)
        }
    }

    override fun getItemCount() = items.size

    private fun bindBanner(holder: BannerVH, item: HomeItem.Banner) {
        val adapter = BannerPagerAdapter(item.items, onVodClick)
        holder.pager.adapter = adapter
    }

    private fun bindCats(holder: CatsVH, item: HomeItem.Cats) {
        holder.chip.text = "分类"
    }

    // 内嵌 RecyclerView 或简化处理
    inner class BannerVH(v: View) : RecyclerView.ViewHolder(v) {
        val pager: ViewPager2 = v as ViewPager2
    }

    inner class CatsVH(val chip: TextView) : RecyclerView.ViewHolder(chip) {
        init {
            chip.setOnClickListener {
                // 点击分类标签不做特别处理
            }
        }
    }

    inner class RowVH(v: View) : RecyclerView.ViewHolder(v) {
        fun bind(item: HomeItem.Row) {
            // 简化为只显示第一个 item
        }
    }

    class BannerPagerAdapter(
        private val items: List<VodSummary>,
        private val onClick: (VodSummary) -> Unit
    ) : RecyclerView.Adapter<BannerPagerVH>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BannerPagerVH {
            val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_banner, parent, false)
            return BannerPagerVH(v)
        }

        override fun onBindViewHolder(holder: BannerPagerVH, position: Int) {
            val item = items[position % items.size]
            holder.title.text = item.vodName
            holder.remarks.text = item.vodRemarks
            if (item.vodPic.isNotBlank()) holder.image.load(item.vodPic)
            holder.itemView.setOnClickListener { onClick(item) }
        }

        override fun getItemCount() = if (items.size <= 1) items.size else Int.MAX_VALUE
    }

    class BannerPagerVH(v: View) : RecyclerView.ViewHolder(v) {
        val image: android.widget.ImageView = v.findViewById(R.id.image)
        val title: TextView = v.findViewById(R.id.title)
        val remarks: TextView = v.findViewById(R.id.remarks)
    }
}
