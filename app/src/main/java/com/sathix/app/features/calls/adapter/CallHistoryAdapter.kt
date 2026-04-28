package com.sathix.app.features.calls.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.sathix.app.databinding.ItemCallBinding
import com.sathix.app.domain.model.Call
import com.sathix.app.domain.model.CallDirection
import com.sathix.app.domain.model.CallStatus
import com.sathix.app.domain.model.CallType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CallHistoryAdapter : ListAdapter<Call, CallHistoryAdapter.VH>(DIFF) {
    private val fmt = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())

    override fun onCreateViewHolder(p: ViewGroup, v: Int): VH =
        VH(ItemCallBinding.inflate(LayoutInflater.from(p.context), p, false))

    override fun onBindViewHolder(h: VH, pos: Int) = h.bind(getItem(pos))

    inner class VH(private val b: ItemCallBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(c: Call) {
            b.tvName.text = if (c.direction == CallDirection.OUTGOING) "To ${c.calleeId}" else "From ${c.callerId}"
            b.tvTime.text = fmt.format(Date(c.startedAt))
            b.tvStatus.text = "${c.status.name.lowercase().replaceFirstChar { it.uppercase() }} • ${c.type.name}"
            b.ivIcon.setImageResource(
                if (c.type == CallType.VIDEO) com.sathix.app.R.drawable.ic_video
                else com.sathix.app.R.drawable.ic_call
            )
            if (c.status == CallStatus.MISSED) b.tvName.setTextColor(0xFFE53935.toInt())
        }
    }

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<Call>() {
            override fun areItemsTheSame(o: Call, n: Call) = o.id == n.id
            override fun areContentsTheSame(o: Call, n: Call) = o == n
        }
    }
}
