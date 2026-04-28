package com.sathix.app.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging
import com.sathix.app.data.remote.FirebasePaths
import com.sathix.app.data.remote.PresenceManager
import com.sathix.app.domain.model.User
import com.sathix.app.domain.repository.AuthRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val db: FirebaseDatabase,
    private val presence: PresenceManager
) : AuthRepository {

    override val currentUserId: String? get() = auth.currentUser?.uid

    override fun observeCurrentUser(): Flow<User?> = callbackFlow {
        val uid = auth.currentUser?.uid
        if (uid == null) { trySend(null); awaitClose { }; return@callbackFlow }
        val ref = db.getReference("${FirebasePaths.USERS}/$uid")
        val l = object : com.google.firebase.database.ValueEventListener {
            override fun onDataChange(s: com.google.firebase.database.DataSnapshot) {
                trySend(s.getValue(User::class.java))
            }
            override fun onCancelled(e: com.google.firebase.database.DatabaseError) {}
        }
        ref.addValueEventListener(l)
        awaitClose { ref.removeEventListener(l) }
    }

    override suspend fun sendOtp(phoneE164: String, activityCallbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks) {
        val opts = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneE164)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setCallbacks(activityCallbacks)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(opts)
    }

    override suspend fun verifyOtp(verificationId: String, code: String): Result<User> = runCatching {
        val credential = PhoneAuthProvider.getCredential(verificationId, code)
        signInWithCredential(credential).getOrThrow()
    }

    override suspend fun signInWithCredential(credential: PhoneAuthCredential): Result<User> = runCatching {
        val res = auth.signInWithCredential(credential).await()
        val fbUser = res.user ?: error("No user")
        val uid = fbUser.uid
        val phone = fbUser.phoneNumber ?: ""
        val ref = db.getReference("${FirebasePaths.USERS}/$uid")
        val existing = ref.get().await().getValue(User::class.java)
        val user = existing ?: User(uid = uid, phone = phone, name = "User")
        ref.setValue(user).await()
        presence.bind(uid)
        runCatching {
            val token = FirebaseMessaging.getInstance().token.await()
            db.getReference("${FirebasePaths.FCM_TOKENS}/$uid").setValue(token).await()
        }
        user
    }

    override suspend fun updateProfile(name: String, about: String, photoUrl: String?): Result<Unit> = runCatching {
        val uid = currentUserId ?: error("Not signed in")
        val updates = mutableMapOf<String, Any?>(
            "name" to name, "about" to about
        )
        if (photoUrl != null) updates["photoUrl"] = photoUrl
        db.getReference("${FirebasePaths.USERS}/$uid").updateChildren(updates).await()
    }

    override suspend fun updateFcmToken(token: String) {
        val uid = currentUserId ?: return
        db.getReference("${FirebasePaths.FCM_TOKENS}/$uid").setValue(token).await()
        db.getReference("${FirebasePaths.USERS}/$uid/fcmToken").setValue(token).await()
    }

    override suspend fun signOut() {
        currentUserId?.let { presence.setOffline(it) }
        auth.signOut()
    }
}
