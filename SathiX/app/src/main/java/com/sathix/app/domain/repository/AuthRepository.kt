package com.sathix.app.domain.repository

import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.sathix.app.domain.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val currentUserId: String?
    fun observeCurrentUser(): Flow<User?>
    suspend fun sendOtp(phoneE164: String, activityCallbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks)
    suspend fun verifyOtp(verificationId: String, code: String): Result<User>
    suspend fun signInWithCredential(credential: PhoneAuthCredential): Result<User>
    suspend fun updateProfile(name: String, about: String, photoUrl: String?): Result<Unit>
    suspend fun updateFcmToken(token: String)
    suspend fun signOut()
}
