package com.sathix.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.sathix.app.domain.model.User

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val uid: String,
    val phone: String,
    val name: String,
    val about: String,
    val photoUrl: String?,
    val online: Boolean,
    val lastSeen: Long,
    val publicKey: String?,
    val fcmToken: String?
) {
    fun toDomain() = User(uid, phone, name, about, photoUrl, online, lastSeen, publicKey, fcmToken)
    companion object {
        fun fromDomain(u: User) = UserEntity(u.uid, u.phone, u.name, u.about, u.photoUrl, u.online, u.lastSeen, u.publicKey, u.fcmToken)
    }
}
