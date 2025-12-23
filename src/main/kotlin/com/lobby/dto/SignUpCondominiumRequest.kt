package com.lobby.dto

import com.lobby.models.Address

data class SignUpCondominiumDto(
    val name: String,
    val cnpj: String,
    val businessEmail: String,
    val businessPhone: String,
    var blocksCount: Int,
    var apartmentCount: Int,
    val address: Address
)