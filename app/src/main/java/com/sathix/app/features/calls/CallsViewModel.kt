package com.sathix.app.features.calls

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sathix.app.domain.repository.CallRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class CallsViewModel @Inject constructor(
    repo: CallRepository
) : ViewModel() {
    val history = repo.observeCallHistory().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}
