package com.lobby.dto

data class ForgotPasswordRequest(
    val email: String
)

data class ResetPasswordRequest(
    val token: String,
    val newPassword: String
)