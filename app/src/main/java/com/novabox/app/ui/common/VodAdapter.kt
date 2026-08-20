package com.novabox.app.ui.common

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.novabox.app.R
import com.novabox.app.data.model.VodSummary

class VodAdapter(
    private val onClick: (VodSummary) -> Unit
) : ListAdapter<VodSummary, VodAdapter.VH>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_vod_card, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = getItem(position)
        holder.name.text = item.vodName
        holder.remarks.text = item.vodRemarks
        if (item.vodPic.isNotBlank()) holder.image.load(item.vodPic)
        holder.itemView.setOnClickListener { onClick(item) }
    }

    class VH(v: android.view.View) : RecyclerView.ViewHolder(v) {
        val image: ImageView = v.findViewById(R.id.image)
        val name: TextView = v.findViewById(R.id.name)
        val remarks: TextView = v.findViewById(R.id.remarks)
    }

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<VodSummary>() {
            override fun areItemsTheSame(a: VodSummary, b: VodSummary) =
                a.vodId == b.vodId && a.sourceId == b.sourceId

            override fun areContentsTheSame(a: VodSummary, b: VodSummary) = a == b
        }
    }
}
