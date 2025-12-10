package com.lobby.extensions

import com.lobby.dto.UserResponse
import com.lobby.models.User

fun User.toResponseDTO(): UserResponse {
    return UserResponse(
        id = this.id,
        profileImageUrl = this.profileImageUrl,
        cpf = this.cpf,
        fullName = this.fullName,
        username = this.username,
        email = this.email,
        phone = this.phone,
        role = this.role.name,
        apartmentNumber = this.apartmentNumber
    )
}