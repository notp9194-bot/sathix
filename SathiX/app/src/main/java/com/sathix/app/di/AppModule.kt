package com.sathix.app.di

import android.content.Context
import androidx.room.Room
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.sathix.app.data.local.AppDatabase
import com.sathix.app.data.local.dao.ChatDao
import com.sathix.app.data.local.dao.MessageDao
import com.sathix.app.data.local.dao.UserDao
import com.sathix.app.data.repository.AuthRepositoryImpl
import com.sathix.app.data.repository.CallRepositoryImpl
import com.sathix.app.data.repository.ChatRepositoryImpl
import com.sathix.app.data.repository.GroupRepositoryImpl
import com.sathix.app.data.repository.StatusRepositoryImpl
import com.sathix.app.domain.repository.AuthRepository
import com.sathix.app.domain.repository.CallRepository
import com.sathix.app.domain.repository.ChatRepository
import com.sathix.app.domain.repository.GroupRepository
import com.sathix.app.domain.repository.StatusRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {
    @Provides @Singleton
    fun provideAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides @Singleton
    fun provideRtdb(): FirebaseDatabase =
        FirebaseDatabase.getInstance("https://callingapp1-f699d-default-rtdb.asia-southeast1.firebasedatabase.app")

    @Provides @Singleton
    fun provideFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()
}

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides @Singleton
    fun provideDb(@ApplicationContext ctx: Context): AppDatabase =
        Room.databaseBuilder(ctx, AppDatabase::class.java, "sathix.db")
            .fallbackToDestructiveMigration().build()

    @Provides fun provideMessageDao(db: AppDatabase): MessageDao = db.messageDao()
    @Provides fun provideChatDao(db: AppDatabase): ChatDao = db.chatDao()
    @Provides fun provideUserDao(db: AppDatabase): UserDao = db.userDao()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds @Singleton abstract fun bindAuth(impl: AuthRepositoryImpl): AuthRepository
    @Binds @Singleton abstract fun bindChat(impl: ChatRepositoryImpl): ChatRepository
    @Binds @Singleton abstract fun bindCall(impl: CallRepositoryImpl): CallRepository
    @Binds @Singleton abstract fun bindStatus(impl: StatusRepositoryImpl): StatusRepository
    @Binds @Singleton abstract fun bindGroup(impl: GroupRepositoryImpl): GroupRepository
}
