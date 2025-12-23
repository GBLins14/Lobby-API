package com.lobby.models

import com.lobby.enums.SubscriptionPlan
import jakarta.persistence.Column
import jakarta.persistence.Embedded
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "condominiums")
data class Condominium (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    var name: String,

    @Column(nullable = false, unique = true, length = 14)
    var cnpj: String,

    @Column(nullable = false, unique = true)
    var businessEmail: String,

    @Column(nullable = false, unique = true)
    var businessPhone: String,

    @Column(nullable = false, unique = true)
    var ownerId: Long,

    @Column(nullable = false, unique = true)
    var condominiumCode: String,

    @Column(nullable = false)
    var blocksCount: Int,

    @Column(nullable = false)
    var apartmentCount: Int,

    @Embedded
    var address: Address,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var subscriptionPlan: SubscriptionPlan,

    var isActive: Boolean = true,

    @Column(nullable = false)
    var createdAt: Instant = Instant.now(),

    var updatedAt: Instant = Instant.now()
)