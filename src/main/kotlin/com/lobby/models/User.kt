package com.lobby.models

import com.lobby.enums.AccountStatus
import com.lobby.enums.Role
import com.fasterxml.jackson.annotation.JsonIgnore
import com.lobby.enums.SubscriptionPlan
import jakarta.persistence.*
import org.hibernate.Hibernate
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import java.time.Instant

@Entity
@Table(name = "users")
data class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false, unique = true)
    val cpf: String,

    var fullName: String? = null,

    @Column(nullable = false, unique = true)
    var username: String,

    @Column(nullable = false, unique = true)
    var email: String,

    @Column(nullable = false, unique = true)
    var phone: String,

    @Enumerated(EnumType.STRING)
    var subscriptionPlan: SubscriptionPlan? = null,

    @Column(unique = true)
    var stripeSubscriptionId: String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "condominium_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    var condominium: Condominium? = null,

    var block: String? = null,

    var apartmentNumber: String? = null,

    @JsonIgnore
    @Column(nullable = false)
    var hashedPassword: String?,

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    var role: Role = Role.RESIDENT,

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    var accountStatus: AccountStatus,

    @Column(nullable = false)
    var banned: Boolean = false,

    var bannedAt: Instant? = null,

    var banExpiresAt: Instant? = null,

    @Column(nullable = false)
    var failedLoginAttempts: Int = 0,

    @Column(nullable = false)
    var tokenVersion: Int = 0,

    @Column(nullable = false)
    val createdAt: Instant = Instant.now(),

    var updatedAt: Instant = Instant.now()
) {
    @PrePersist
    @PreUpdate
    fun formatData() {
        this.fullName = this.fullName?.uppercase()
        this.username = this.username.lowercase()
        this.email = this.email.lowercase()
        this.apartmentNumber = this.apartmentNumber?.uppercase()?.replace(Regex("[^A-Z0-9]"), "")
    }

    fun isBanExpired(): Boolean {
        if (banExpiresAt == null) return false
        return Instant.now().isAfter(banExpiresAt)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false
        other as User
        return id != 0L && id == other.id
    }

    override fun hashCode(): Int = javaClass.hashCode()

    override fun toString(): String {
        return "User(id=$id, username='$username', email='$email', role=$role)"
    }
}
