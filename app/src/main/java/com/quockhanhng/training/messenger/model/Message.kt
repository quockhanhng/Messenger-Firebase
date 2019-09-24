package com.quockhanhng.training.messenger.model

import java.util.*

data class Message(
    var messageUser: String = "",
    var messageText: String = "",
    var messageUserId: String = "",
    var messageLikesCount: Long = 0,
    var messageLikes: Map<String, Boolean>? = null,
    var messageTime: Long = Date().time
) {
}