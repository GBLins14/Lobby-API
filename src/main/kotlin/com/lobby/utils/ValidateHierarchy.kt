package com.lobby.utils

import com.lobby.enums.Role
import com.lobby.exceptions.ConflictException
import com.lobby.exceptions.UnauthorizedException
import com.lobby.models.User

fun validateHierarchy(user: User, targetAccount: User) {
    if (targetAccount == user) {
        throw ConflictException("Você não pode editar sua própria conta enquanto logado.")
    }

    if (user.role == Role.SYNDIC) {
        if (targetAccount.role == Role.BUSINESS || targetAccount.role == Role.SYNDIC) {
            throw UnauthorizedException("Você não tem permissão para gerenciar um usuário com cargo igual ou superior ao seu.")
        }
    } else if (targetAccount.role == Role.BUSINESS) {
        throw UnauthorizedException("Você não tem permissão para gerenciar um usuário com cargo igual ou superior ao seu.")
    }
}