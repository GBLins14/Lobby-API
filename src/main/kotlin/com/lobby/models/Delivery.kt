package com.lobby.models

import com.lobby.enums.DeliveryStatus
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import java.time.LocalDateTime


@Entity
@Table(name = "deliveries")
data class Delivery(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    val trackingCode: String, // Código de rastreio ou "IFOOD-123"

    @ManyToOne
    @JoinColumn(name = "resident_id", nullable = false)
    val resident: User, // Quem recebe (Morador)

    @ManyToOne
    @JoinColumn(name = "doorman_id", nullable = false)
    val doorman: User, // Quem recebeu na portaria (Porteiro)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: DeliveryStatus = DeliveryStatus.WAITING_PICKUP,

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    val arrivalDate: LocalDateTime = LocalDateTime.now(),

    var withdrawalDate: LocalDateTime? = null
)
