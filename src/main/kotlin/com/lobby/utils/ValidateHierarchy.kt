package com.lobby.utils

import com.lobby.enums.Role
import com.lobby.exceptions.ConflictException
import com.lobby.exceptions.UnauthorizedException
import com.lobby.models.User

fun validateHierarchy(syndicAccount: User, targetAccount: User) {
    if (targetAccount == syndicAccount) {
        throw ConflictException("Você não pode editar sua própria conta enquanto logado.")
    }

    if (targetAccount.role == Role.BUSINESS || targetAccount.role == Role.SYNDIC) {
        throw UnauthorizedException("Você não tem permissão para gerenciar um usuário com cargo igual ou superior ao seu.")
    }
}