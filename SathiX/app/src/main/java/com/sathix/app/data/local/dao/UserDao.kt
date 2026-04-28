package com.sathix.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sathix.app.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(user: UserEntity)

    @Query("SELECT * FROM users WHERE uid = :uid")
    suspend fun get(uid: String): UserEntity?

    @Query("SELECT * FROM users WHERE uid = :uid")
    fun observe(uid: String): Flow<UserEntity?>

    @Query("SELECT * FROM users")
    fun observeAll(): Flow<List<UserEntity>>
}
