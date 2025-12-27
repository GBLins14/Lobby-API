package com.lobby.models

import com.lobby.enums.DeliveryStatus
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.PrePersist
import jakarta.persistence.PreUpdate
import jakarta.persistence.Table
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import java.time.Instant

@Entity
@Table(name = "deliveries")
data class Delivery(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "condominium_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    val condominium: Condominium,

    @Column(nullable = false, unique = true)
    val trackingCode: String,

    @Column(nullable = false)
    val recipientName: String,

    var apartmentNumber: String? = null,

    @ManyToOne
    @JoinColumn(name = "doorman_id", nullable = false)
    val doorman: User,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: DeliveryStatus = DeliveryStatus.WAITING_PICKUP,

    @Column(nullable = false, updatable = false)
    val arrivalDate: Instant = Instant.now(),

    var withdrawalDate: Instant? = null
) {
    @PrePersist
    @PreUpdate
    fun formatData() {
        this.apartmentNumber = this.apartmentNumber?.uppercase()?.replace(Regex("[^A-Z0-9]"), "")
    }
}

