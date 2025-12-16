package com.lobby.repositories

import com.lobby.models.PasswordResetToken
import com.lobby.models.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface TokenRepository : JpaRepository<PasswordResetToken, UUID> {
    fun findByToken(token: String): PasswordResetToken?
    fun findByUser(user: User): PasswordResetToken?
    fun deleteByUser(user: User)
}