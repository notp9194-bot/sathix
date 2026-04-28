package com.sathix.app.features.auth

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.FirebaseException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.sathix.app.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

data class AuthUiState(
    val loading: Boolean = false,
    val otpSent: Boolean = false,
    val signedIn: Boolean = false,
    val message: String? = null
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepo: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState = _uiState.asStateFlow()

    private var verificationId: String? = null

    fun isSignedIn(): Boolean = authRepo.currentUserId != null

    fun sendOtp(activity: Activity, phoneE164: String) {
        _uiState.update { it.copy(loading = true, message = "Sending OTP...") }
        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                viewModelScope.launch { signIn(credential) }
            }
            override fun onVerificationFailed(e: FirebaseException) {
                _uiState.update { it.copy(loading = false, message = e.localizedMessage ?: "Failed") }
            }
            override fun onCodeSent(id: String, token: PhoneAuthProvider.ForceResendingToken) {
                verificationId = id
                _uiState.update { it.copy(loading = false, otpSent = true, message = "OTP sent") }
            }
        }
        val opts = PhoneAuthOptions.newBuilder()
            .setPhoneNumber(phoneE164)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(callbacks)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(opts)
    }

    fun verifyOtp(code: String) {
        val id = verificationId ?: return
        _uiState.update { it.copy(loading = true, message = "Verifying...") }
        viewModelScope.launch {
            val credential = PhoneAuthProvider.getCredential(id, code)
            signIn(credential)
        }
    }

    private suspend fun signIn(credential: PhoneAuthCredential) {
        val result = authRepo.signInWithCredential(credential)
        result.onSuccess {
            _uiState.update { s -> s.copy(loading = false, signedIn = true, message = "Welcome ${it.name}") }
        }.onFailure { e ->
            _uiState.update { s -> s.copy(loading = false, message = e.localizedMessage ?: "Sign in failed") }
        }
    }
}
