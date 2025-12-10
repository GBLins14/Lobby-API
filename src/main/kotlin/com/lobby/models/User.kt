package com.lobby.models

import com.lobby.enums.AccountStatus
import com.lobby.enums.Role
import com.fasterxml.jackson.annotation.JsonIgnore
import org.springframework.format.annotation.DateTimeFormat
import java.time.LocalDateTime
import jakarta.persistence.*

@Entity
@Table(name = "users")
data class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(length = 500)
    var profileImageUrl: String? = null,

    @Column(nullable = false, unique = true)
    val cpf: String,

    var fullName: String? = null,

    @Column(nullable = false, unique = true)
    var username: String,

    @Column(nullable = false, unique = true)
    var email: String,

    @Column(nullable = false, unique = true)
    var phone: String,

    @JsonIgnore
    @Column(nullable = false)
    var hashedPassword: String?,

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    var role: Role = Role.RESIDENT,

    var apartmentNumber: Int? = null,

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    var accountStatus: AccountStatus,

    @Column(nullable = false)
    var banned: Boolean = false,

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    var bannedAt: LocalDateTime? = null,

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    var banExpiresAt: LocalDateTime? = null,

    @Column(nullable = false)
    var failedLoginAttempts: Int = 0,

    @Column(nullable = false)
    var tokenVersion: Int = 0,

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    var createdAt: LocalDateTime = LocalDateTime.now(),

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    var updatedAt: LocalDateTime = LocalDateTime.now()
)
