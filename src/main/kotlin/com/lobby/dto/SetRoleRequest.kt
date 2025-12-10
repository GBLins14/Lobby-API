package com.lobby.dto

import com.lobby.enums.Role

data class SetRoleDto(
    val login: String,
    val role: Role
)