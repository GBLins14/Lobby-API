package com.lobby.dto

data class CreateDeliveryDto(
    val recipientName: String,
    val block: String,
    val apartmentNumber: String,
)