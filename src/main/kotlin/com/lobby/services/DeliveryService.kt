package com.lobby.services

import com.lobby.dto.toResponse
import com.lobby.repositories.DeliveryRepository
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service

@Service
class DeliveryService(
    private val deliveryRepository: DeliveryRepository
) {
    fun listMyDeliveries(apartmentNumber: String?): ResponseEntity<Any> {
        if (apartmentNumber == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(mapOf("success" to false, "message" to "Nenhuma encomenda encontrada."))
        }

        val apartmentNumber = apartmentNumber.uppercase().replace(Regex("[^A-Z0-9]"), "")

        val deliveries = deliveryRepository.findByApartmentNumber(apartmentNumber)
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(mapOf("success" to false, "message" to "Nenhuma encomenda encontrada."))

        val response = deliveries.map { it.toResponse() }

        return ResponseEntity.ok(mapOf("success" to true, "deliveries" to response))
    }
}