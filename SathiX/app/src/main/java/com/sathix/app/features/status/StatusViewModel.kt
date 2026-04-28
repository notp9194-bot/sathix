package com.sathix.app.features.status

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sathix.app.domain.model.StatusType
import com.sathix.app.domain.repository.StatusRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StatusViewModel @Inject constructor(
    private val repo: StatusRepository
) : ViewModel() {
    val statuses = repo.observeStatuses().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())

    fun postTextStatus(text: String) {
        viewModelScope.launch { repo.postStatus(StatusType.TEXT, text) }
    }
}
