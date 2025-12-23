package com.lobby.services

import com.lobby.dto.toResponse
import com.lobby.extensions.error
import com.lobby.models.Condominium
import com.lobby.repositories.DeliveryRepository
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service

@Service
class DeliveryService(
    private val deliveryRepository: DeliveryRepository
) {
    fun listMyDeliveries(condominium: Condominium, apartmentNumber: String): ResponseEntity<Any> {
        val apartmentNumber = apartmentNumber.uppercase().replace(Regex("[^A-Z0-9]"), "")

        val deliveries = deliveryRepository.findByCondominiumAndApartmentNumber(condominium, apartmentNumber)

        val response = deliveries.map { it.toResponse() }

        return ResponseEntity.ok(mapOf("success" to true, "deliveries" to response))
    }
}