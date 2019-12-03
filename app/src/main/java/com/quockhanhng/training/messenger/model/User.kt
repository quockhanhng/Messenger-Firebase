package com.quockhanhng.training.messenger.model

import java.io.Serializable

data class User(
    var userId: String = "",
    var surname: String = "",
    var name: String = "",
    var email: String = "",
    var phoneNumber: String = "",
    var dob: String = "",
    var gender: String = "",
    var profileImgUrl: String? = null
) : Serializable {
}