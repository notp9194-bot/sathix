package com.sathix.app.features.chat.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.sathix.app.databinding.ItemMessageInBinding
import com.sathix.app.databinding.ItemMessageOutBinding
import com.sathix.app.domain.model.Message
import com.sathix.app.domain.model.MessageStatus
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MessageAdapter(
    private val currentUserIdProvider: () -> String?
) : ListAdapter<Message, RecyclerView.ViewHolder>(DIFF) {

    private val timeFmt = SimpleDateFormat("HH:mm", Locale.getDefault())

    override fun getItemViewType(position: Int): Int =
        if (getItem(position).senderId == currentUserIdProvider()) TYPE_OUT else TYPE_IN

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == TYPE_OUT) {
            OutVH(ItemMessageOutBinding.inflate(inflater, parent, false))
        } else {
            InVH(ItemMessageInBinding.inflate(inflater, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val msg = getItem(position)
        when (holder) {
            is OutVH -> holder.bind(msg)
            is InVH -> holder.bind(msg)
        }
    }

    inner class OutVH(private val b: ItemMessageOutBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(m: Message) {
            b.tvText.text = if (m.deletedForEveryone) "This message was deleted" else m.content
            b.tvTime.text = timeFmt.format(Date(m.timestamp))
            b.ivStatus.setImageResource(
                when (m.status) {
                    MessageStatus.PENDING -> com.sathix.app.R.drawable.ic_status_pending
                    MessageStatus.SENT -> com.sathix.app.R.drawable.ic_status_sent
                    MessageStatus.DELIVERED -> com.sathix.app.R.drawable.ic_status_delivered
                    MessageStatus.SEEN -> com.sathix.app.R.drawable.ic_status_seen
                    MessageStatus.FAILED -> com.sathix.app.R.drawable.ic_status_failed
                }
            )
        }
    }

    inner class InVH(private val b: ItemMessageInBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(m: Message) {
            b.tvText.text = if (m.deletedForEveryone) "This message was deleted" else m.content
            b.tvTime.text = timeFmt.format(Date(m.timestamp))
        }
    }

    companion object {
        const val TYPE_IN = 0
        const val TYPE_OUT = 1
        val DIFF = object : DiffUtil.ItemCallback<Message>() {
            override fun areItemsTheSame(o: Message, n: Message) = o.id == n.id
            override fun areContentsTheSame(o: Message, n: Message) = o == n
        }
    }
}
