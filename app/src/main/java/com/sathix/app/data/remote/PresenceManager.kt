package com.sathix.app.data.remote

import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PresenceManager @Inject constructor(
    private val db: FirebaseDatabase
) {
    fun bind(uid: String) {
        val online = db.getReference(FirebasePaths.userPresence(uid))
        val infoConnected = db.getReference(".info/connected")
        infoConnected.addValueEventListener(object : com.google.firebase.database.ValueEventListener {
            override fun onDataChange(s: com.google.firebase.database.DataSnapshot) {
                if (s.getValue(Boolean::class.java) == true) {
                    online.onDisconnect().setValue(
                        mapOf("online" to false, "lastSeen" to ServerValue.TIMESTAMP)
                    )
                    online.setValue(mapOf("online" to true, "lastSeen" to ServerValue.TIMESTAMP))
                }
            }
            override fun onCancelled(e: com.google.firebase.database.DatabaseError) {}
        })
    }

    fun setOffline(uid: String) {
        db.getReference(FirebasePaths.userPresence(uid))
            .setValue(mapOf("online" to false, "lastSeen" to ServerValue.TIMESTAMP))
    }
}
