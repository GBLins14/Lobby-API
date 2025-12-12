package com.lobby.dto

data class CreateDeliveryDto(
    val recipientName: String,
    val apartmentNumber: String? = null,
)