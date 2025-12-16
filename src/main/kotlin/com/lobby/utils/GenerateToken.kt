package com.lobby.utils

import java.security.SecureRandom
import java.util.Base64

fun generateToken(): String {
    val random = SecureRandom()
    val bytes = ByteArray(32)
    random.nextBytes(bytes)
    val token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)

    return token
}