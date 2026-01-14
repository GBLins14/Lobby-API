package com.lobby.models

import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "analytics")
data class Analytic(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val countId: Long = 0,

    val source: String,

    val page: String,

    val userIp: String,

    val createdAt: Instant = Instant.now()
)