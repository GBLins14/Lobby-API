package com.lobby.repositories

import com.lobby.enums.AccountStatus
import com.lobby.models.Condominium
import com.lobby.models.User
import org.springframework.data.jpa.repository.JpaRepository

interface AccountRepository : JpaRepository<User, Long> {
    fun findByUsernameOrEmail(username: String, email: String): User?
    fun findByCpf(cpf: String): User?
    fun findByUsername(username: String): User?
    fun findByEmail(email: String): User?
    fun findByPhone(phone: String): User?

    fun findAllByCondominium(condominium: Condominium): List<User>
    fun findByCondominiumAndId(condominium: Condominium, accountId: Long): User?
    fun findByCondominiumAndUsernameOrEmail(condominium: Condominium, username: String, email: String): User?
    fun findByCondominiumAndBanned(condominium: Condominium, banned: Boolean): List<User>?
    fun findByCondominiumAndAccountStatus(condominium: Condominium, accountStatus: AccountStatus): List<User>?
    fun findByCondominiumAndApartmentNumber(condominium: Condominium, apartmentNumber: String): List<User>?
}
