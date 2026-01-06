package com.lobby.repositories

import com.lobby.enums.AccountStatus
import com.lobby.models.Condominium
import com.lobby.models.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface AccountRepository : JpaRepository<User, Long> {
    fun findByStripeSubscriptionId(stripeSubscriptionId: String): User?
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
    fun findByCondominiumAndBlockAndApartmentNumber(condominium: Condominium, block: String, apartmentNumber: String): List<User>?
    fun countByCondominiumAndBlockAndApartmentNumber(condominium: Condominium, block: String, apartmentNumber: String): Long
    @Query("""
        SELECT COUNT(DISTINCT CONCAT(u.block, '-', u.apartmentNumber)) 
        FROM User u 
        WHERE u.condominium = :condominium 
        AND u.block IS NOT NULL 
        AND u.apartmentNumber IS NOT NULL
    """)
    fun countTotalUnits(@Param("condominium") condominium: Condominium): Long
}
