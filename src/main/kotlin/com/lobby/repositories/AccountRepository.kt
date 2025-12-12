package com.lobby.repositories

import com.lobby.enums.AccountStatus
import com.lobby.models.User
import org.springframework.data.jpa.repository.JpaRepository

interface AccountRepository : JpaRepository<User, Long> {
    fun findByUsernameOrEmail(username: String, email: String): User?
    fun findByCpf(cpf: String): User?
    fun findByUsername(username: String): User?
    fun findByEmail(email: String): User?
    fun findByPhone(phone: String): User?
    fun findByBanned(banned: Boolean): List<User>?
    fun findByAccountStatus(accountStatus: AccountStatus): List<User>?
    fun findByApartmentNumber(apartmentNumber: String): List<User>?
}
