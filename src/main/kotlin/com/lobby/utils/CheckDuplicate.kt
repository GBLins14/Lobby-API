package com.lobby.utils

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity

fun checkDuplicate(value: Any?, message: String): ResponseEntity<Any>? {
    return if (value != null) ResponseEntity.status(HttpStatus.CONFLICT)
        .body(mapOf("success" to false, "message" to message)) else null
}