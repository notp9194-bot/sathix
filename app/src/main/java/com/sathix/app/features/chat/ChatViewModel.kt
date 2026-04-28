package com.sathix.app.features.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sathix.app.domain.model.Message
import com.sathix.app.domain.repository.AuthRepository
import com.sathix.app.domain.repository.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepo: ChatRepository,
    private val authRepo: AuthRepository
) : ViewModel() {

    private val chatIdFlow = MutableStateFlow<String?>(null)
    val currentUserId: String? get() = authRepo.currentUserId

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val messages = chatIdFlow.flatMapLatest { id ->
        if (id == null) kotlinx.coroutines.flow.flowOf(emptyList<Message>())
        else chatRepo.observeMessages(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun bind(chatId: String) { chatIdFlow.value = chatId }

    fun send(text: String) {
        val id = chatIdFlow.value ?: return
        viewModelScope.launch { chatRepo.sendMessage(id, text) }
    }

    fun setTyping(typing: Boolean) {
        val id = chatIdFlow.value ?: return
        viewModelScope.launch { chatRepo.setTyping(id, typing) }
    }
}
