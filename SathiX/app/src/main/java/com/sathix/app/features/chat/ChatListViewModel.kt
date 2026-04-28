package com.sathix.app.features.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sathix.app.domain.repository.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ChatListViewModel @Inject constructor(
    repo: ChatRepository
) : ViewModel() {
    val chats = repo.observeChats().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}
