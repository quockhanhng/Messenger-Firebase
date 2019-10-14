package com.quockhanhng.training.messenger.model

import java.util.*
import kotlin.collections.ArrayList

data class Message(
    var messageId: String = "",
    var messageUser: String = "",
    var messageText: String = "",
    var messageUserId: String = "",
    var messageLikesCount: Long = 0,
    var messageLikes: ArrayList<String>? = null,
    var messageTime: Long = Date().time
) {
}