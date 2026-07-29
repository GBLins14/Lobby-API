package com.lobby.models

import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "analytics")
data class Analytic(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val countId: Long = 0,

    val source: String? = null,

    val page: String? = null,

    val userIp: String? = null,

    val event: String? = null,

    val createdAt: Instant = Instant.now()
)
