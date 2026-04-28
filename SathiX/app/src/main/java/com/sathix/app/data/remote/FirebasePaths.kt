package com.sathix.app.data.remote

object FirebasePaths {
    const val USERS = "users"
    const val CHATS = "chats"
    const val MESSAGES = "messages"
    const val TYPING = "typing"
    const val PRESENCE = "presence"
    const val CALLS = "calls"
    const val SIGNALING = "signaling"
    const val STATUSES = "statuses"
    const val GROUPS = "groups"
    const val COMMUNITIES = "communities"
    const val CHANNELS = "channels"
    const val FCM_TOKENS = "fcmTokens"

    fun userMessages(chatId: String) = "$MESSAGES/$chatId"
    fun userChats(uid: String) = "$USERS/$uid/chats"
    fun userPresence(uid: String) = "$PRESENCE/$uid"
    fun userTyping(chatId: String, uid: String) = "$TYPING/$chatId/$uid"
    fun userStatuses(uid: String) = "$STATUSES/$uid"
}
