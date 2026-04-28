package com.sathix.app.features.chat

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.sathix.app.databinding.FragmentChatListBinding
import com.sathix.app.features.chat.adapter.ChatListAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ChatListFragment : Fragment() {

    private var _binding: FragmentChatListBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ChatListViewModel by viewModels()
    private lateinit var adapter: ChatListAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, s: Bundle?): View {
        _binding = FragmentChatListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, s: Bundle?) {
        adapter = ChatListAdapter { chat ->
            startActivity(Intent(requireContext(), ChatActivity::class.java).apply {
                putExtra("chatId", chat.id)
                putExtra("chatName", chat.name)
            })
        }
        binding.rv.layoutManager = LinearLayoutManager(requireContext())
        binding.rv.adapter = adapter
        binding.rv.setHasFixedSize(true)

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.chats.collectLatest { adapter.submitList(it) }
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
