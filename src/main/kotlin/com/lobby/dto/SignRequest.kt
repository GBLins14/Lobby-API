package com.lobby.dto

import com.lobby.enums.Role

data class SignUpDto(
    val cpf: String,
    val fullName: String,
    val username: String,
    val email: String,
    val phone: String,
    val password: String,
    val role: Role? = Role.RESIDENT
)

data class SignInDto(
    val login: String,
    val password: String
)