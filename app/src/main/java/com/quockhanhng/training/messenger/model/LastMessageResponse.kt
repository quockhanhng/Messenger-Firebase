package com.quockhanhng.training.messenger.model

import java.io.Serializable

data class LastMessageResponse(
    var user: User,
    var lastMessageTime: Long
) : Serializable {
}