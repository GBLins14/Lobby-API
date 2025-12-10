package com.lobby.dto

data class UserResponse(
    val id: Long,
    val profileImageUrl: String?,
    val cpf: String,
    val fullName: String?,
    val username: String,
    val email: String,
    val phone: String,
    val role: String,
    val apartmentNumber: Int?
)
