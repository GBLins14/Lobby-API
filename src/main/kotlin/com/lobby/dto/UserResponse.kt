package com.lobby.dto

import com.lobby.enums.AccountStatus
import com.lobby.models.Condominium
import com.lobby.models.User

data class UserResponse(
    val id: Long,
    val cpf: String,
    val fullName: String?,
    val username: String,
    val email: String,
    val phone: String,
    val condominium: Condominium?,
    val block: String?,
    val apartmentNumber: String?,
    val role: String,
    val accountStatus: AccountStatus
)
fun User.toResponseDTO(): UserResponse {
    return UserResponse(
        id = this.id,
        cpf = this.cpf,
        fullName = this.fullName,
        username = this.username,
        email = this.email,
        phone = this.phone,
        condominium = this.condominium,
        block = this.block,
        apartmentNumber = this.apartmentNumber,
        role = this.role.name,
        accountStatus = this.accountStatus
    )
}