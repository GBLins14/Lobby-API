package com.lobby.dto

import com.lobby.enums.DeliveryStatus
import com.lobby.models.Delivery
import java.time.LocalDateTime

data class DeliveryResponseDto(
    val id: Long,
    val status: DeliveryStatus,
    val recipientName: String,
    val apartmentNumber: String?,
    val doormanId: Long,
    val arrivalDate: LocalDateTime
)
fun Delivery.toResponse(): DeliveryResponseDto {
    return DeliveryResponseDto(
        id = this.id,
        status = this.status,
        recipientName = this.recipientName,
        apartmentNumber = this.apartmentNumber,
        doormanId = this.doorman.id,
        arrivalDate = this.arrivalDate
    )
}