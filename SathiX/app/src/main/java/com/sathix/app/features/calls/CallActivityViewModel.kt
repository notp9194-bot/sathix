package com.sathix.app.features.calls

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sathix.app.domain.repository.CallRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CallActivityViewModel @Inject constructor(
    private val repo: CallRepository
) : ViewModel() {
    fun endCall(callId: String) { viewModelScope.launch { repo.endCall(callId) } }
}
