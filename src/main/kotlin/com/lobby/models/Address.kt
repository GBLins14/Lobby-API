package com.lobby.models

import jakarta.persistence.Column
import jakarta.persistence.Embeddable

@Embeddable
data class Address(
    @Column(nullable = false, length = 9)
    var zipCode: String,

    @Column(nullable = false)
    var street: String,

    @Column(nullable = false)
    var number: String,

    @Column(nullable = true)
    var complement: String? = null,

    @Column(nullable = false)
    var neighborhood: String,

    @Column(nullable = false)
    var city: String,

    @Column(nullable = false, length = 2)
    var state: String
)