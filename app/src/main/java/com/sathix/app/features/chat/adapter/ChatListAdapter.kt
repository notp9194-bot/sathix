package com.sathix.app.features.chat.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.sathix.app.R
import com.sathix.app.databinding.ItemChatBinding
import com.sathix.app.domain.model.Chat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ChatListAdapter(
    private val onClick: (Chat) -> Unit
) : ListAdapter<Chat, ChatListAdapter.VH>(DIFF) {

    private val timeFmt = SimpleDateFormat("HH:mm", Locale.getDefault())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val b = ItemChatBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(b)
    }

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(getItem(position))

    inner class VH(private val b: ItemChatBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(chat: Chat) {
            b.tvName.text = chat.name
            b.tvLastMessage.text = chat.lastMessage
            b.tvTime.text = if (chat.lastMessageTime > 0) timeFmt.format(Date(chat.lastMessageTime)) else ""
            if (chat.unreadCount > 0) {
                b.tvBadge.visibility = android.view.View.VISIBLE
                b.tvBadge.text = chat.unreadCount.toString()
            } else b.tvBadge.visibility = android.view.View.GONE
            Glide.with(b.ivAvatar).load(chat.photoUrl).placeholder(R.drawable.ic_person).circleCrop().into(b.ivAvatar)
            b.root.setOnClickListener { onClick(chat) }
        }
    }

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<Chat>() {
            override fun areItemsTheSame(o: Chat, n: Chat) = o.id == n.id
            override fun areContentsTheSame(o: Chat, n: Chat) = o == n
        }
    }
}
