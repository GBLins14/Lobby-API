package com.lobby.dto

import com.lobby.enums.DeliveryStatus
import com.lobby.models.Delivery
import java.time.LocalDateTime

data class DeliveryResponseDto(
    val id: Long,
    val status: DeliveryStatus,
    val residentId: Long, // <--- AQUI VAI SÓ O ID
    val doormanId: Long,  // <--- AQUI VAI SÓ O ID
    val arrivalDate: LocalDateTime
)
fun Delivery.toResponse(): DeliveryResponseDto {
    return DeliveryResponseDto(
        id = this.id,
        status = this.status,
        residentId = this.resident.id, // Extrai só o ID do objeto User
        doormanId = this.doorman.id,   // Extrai só o ID do objeto User
        arrivalDate = this.arrivalDate
    )
}