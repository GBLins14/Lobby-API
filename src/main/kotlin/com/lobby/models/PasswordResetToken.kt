package com.lobby.models

import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "password_reset_tokens")
class PasswordResetToken(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @Column(nullable = false, unique = true)
    val token: String,

    @OneToOne(targetEntity = User::class, fetch = FetchType.EAGER)
    @JoinColumn(nullable = false, name = "user_id")
    val user: User,

    @Column(nullable = false)
    val expiryDate: LocalDateTime
) {
    fun isExpired(): Boolean {
        return LocalDateTime.now().isAfter(expiryDate)
    }
}