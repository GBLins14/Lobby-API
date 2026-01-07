package com.lobby.utils

import com.lobby.models.User
import com.lobby.repositories.AccountRepository
import com.lobby.repositories.CondominiumRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Component

@Component
class CancelLocalPlan(private val condominiumRepository: CondominiumRepository, private val accountRepository: AccountRepository) {
    @Transactional
    fun cancel(user: User) {
        user.subscriptionPlan = null
        user.stripeSubscriptionId = null

        accountRepository.save(user)

        user.condominium?.let {
            condominiumRepository.delete(it)
        }
    }
}
