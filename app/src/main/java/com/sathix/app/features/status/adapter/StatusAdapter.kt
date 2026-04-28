package com.sathix.app.features.status.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.sathix.app.databinding.ItemStatusBinding
import com.sathix.app.domain.model.Status

class StatusAdapter : ListAdapter<Map.Entry<String, List<Status>>, StatusAdapter.VH>(DIFF) {
    override fun onCreateViewHolder(p: ViewGroup, v: Int): VH =
        VH(ItemStatusBinding.inflate(LayoutInflater.from(p.context), p, false))

    override fun onBindViewHolder(h: VH, pos: Int) = h.bind(getItem(pos))

    inner class VH(private val b: ItemStatusBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(e: Map.Entry<String, List<Status>>) {
            b.tvName.text = e.key
            b.tvCount.text = "${e.value.size} updates"
        }
    }

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<Map.Entry<String, List<Status>>>() {
            override fun areItemsTheSame(o: Map.Entry<String, List<Status>>, n: Map.Entry<String, List<Status>>) = o.key == n.key
            override fun areContentsTheSame(o: Map.Entry<String, List<Status>>, n: Map.Entry<String, List<Status>>) = o.value == n.value
        }
    }
}
