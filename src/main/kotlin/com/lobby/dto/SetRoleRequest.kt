package com.lobby.dto

import com.lobby.enums.Role

data class SetRoleDto(
    val id: Long,
    val role: Role
)