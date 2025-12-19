package com.lobby.dto

import com.lobby.enums.DeliveryStatus
import com.lobby.models.Delivery
import java.time.Instant

data class DeliveryResponseDto(
    val id: Long,
    val status: DeliveryStatus,
    val trackingCode: String,
    val recipientName: String,
    val apartmentNumber: String?,
    val doormanId: Long,
    val arrivalDate: Instant,
    val withdrawalDate: Instant?
)

data class DeliveryListDto(
    val id: Long,
    val status: DeliveryStatus,
    val recipientName: String,
    val apartmentNumber: String?,
    val doormanId: Long,
    val arrivalDate: Instant,
    val withdrawalDate: Instant?
)

fun Delivery.toResponse(): DeliveryResponseDto {
    return DeliveryResponseDto(
        id = this.id,
        status = this.status,
        trackingCode = this.trackingCode,
        recipientName = this.recipientName,
        apartmentNumber = this.apartmentNumber,
        doormanId = this.doorman.id,
        arrivalDate = this.arrivalDate,
        withdrawalDate = this.withdrawalDate
    )
}

fun Delivery.toListResponse(): DeliveryListDto {
    return DeliveryListDto(
        id = this.id,
        status = this.status,
        recipientName = this.recipientName,
        apartmentNumber = this.apartmentNumber,
        doormanId = this.doorman.id,
        arrivalDate = this.arrivalDate,
        withdrawalDate = this.withdrawalDate
    )
}