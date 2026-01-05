package com.lobby.utils

import com.lobby.exceptions.NotFoundException
import com.lobby.models.Condominium
import com.lobby.models.User
import com.lobby.repositories.AccountRepository
import org.springframework.stereotype.Component

@Component
class FindAccountOrThrow(private val accountRepository: AccountRepository) {
    fun findAccount(condominium: Condominium, id: Long): User {
        return accountRepository.findByCondominiumAndId(condominium, id)
            ?: throw NotFoundException("Conta não encontrada.")
    }
}