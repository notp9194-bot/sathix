package com.sathix.app.features.chat

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.sathix.app.databinding.ActivityChatBinding
import com.sathix.app.features.chat.adapter.MessageAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding
    private val viewModel: ChatViewModel by viewModels()
    private lateinit var adapter: MessageAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val chatId = intent.getStringExtra("chatId") ?: return finish()
        val chatName = intent.getStringExtra("chatName") ?: ""
        binding.toolbar.title = chatName
        binding.toolbar.setNavigationOnClickListener { finish() }

        adapter = MessageAdapter(currentUserIdProvider = { viewModel.currentUserId })
        val lm = LinearLayoutManager(this).apply { stackFromEnd = true }
        binding.rv.layoutManager = lm
        binding.rv.adapter = adapter
        binding.rv.setHasFixedSize(true)
        binding.rv.itemAnimator = null

        viewModel.bind(chatId)

        binding.btnSend.setOnClickListener {
            val text = binding.etInput.text.toString().trim()
            if (text.isNotEmpty()) {
                viewModel.send(text)
                binding.etInput.setText("")
            }
        }

        binding.etInput.setOnFocusChangeListener { _, focused -> viewModel.setTyping(focused) }

        lifecycleScope.launch {
            viewModel.messages.collectLatest { list ->
                adapter.submitList(list) {
                    if (list.isNotEmpty()) binding.rv.scrollToPosition(list.size - 1)
                }
            }
        }
    }
}
