package com.lobby.utils

import com.lobby.exceptions.ConflictException

fun checkDuplicate(value: Any?, message: String) {
    if (value != null) throw ConflictException(message)
}